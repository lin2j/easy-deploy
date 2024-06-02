package tech.lin2j.idea.plugin.runner;

import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.BooleanTableCellEditor;
import com.intellij.ui.BooleanTableCellRenderer;
import com.intellij.ui.TableUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.FormBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.domain.model.Command;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.DeployProfile;
import tech.lin2j.idea.plugin.domain.model.NoneCommand;
import tech.lin2j.idea.plugin.domain.model.UploadProfile;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ui.dialog.SelectUploadProfileDialog;
import tech.lin2j.idea.plugin.ui.table.DeployProfileTableModel;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author linjinjia
 * @date 2024/4/24 21:57
 */
public class DeploySettingsEditor extends SettingsEditor<DeployRunConfiguration> {

    private final JPanel myPanel;
    private JBTable deployProfileTable;
    private List<DeployProfile> deployProfiles = new ArrayList<>();
    private DeployProfileTableModel deployProfileTableModel;

    public DeploySettingsEditor() {
        initDeployProfileTable();

        myPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Deploy profile", getDeployProfilePanel(), true)
                .getPanel();
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return myPanel;
    }

    @Override
    protected void resetEditorFrom(DeployRunConfiguration deployRunConfiguration) {
        deployProfiles = deployRunConfiguration.getDeployProfile();
        if (deployProfiles.size() > 0) {
            initDeployProfileTable();
        }
    }

    @Override
    protected void applyEditorTo(@NotNull DeployRunConfiguration deployRunConfiguration) {
        deployRunConfiguration.setDeployProfiles(deployProfiles);
    }

    private JPanel getDeployProfilePanel() {
        return ToolbarDecorator.createDecorator(deployProfileTable)
                .setPreferredSize(new Dimension(0, 200))
                .setToolbarPosition(ActionToolbarPosition.TOP)
                .setAddAction(e -> {
                    StringBuilder selectedProfile = new StringBuilder();
                    new SelectUploadProfileDialog(selectedProfile).showAndGet();
                    if (StringUtil.isEmpty(selectedProfile)) {
                        return;
                    }
                    // check repeat
                    for (DeployProfile dp : this.deployProfiles) {
                        String cmpStr = (dp.isActive() ? 1 : 0) + "@" + selectedProfile;
                        if (Objects.equals(cmpStr, dp.toString())) {
                            String msg = MessagesBundle.getText("runner.editor.tip.profile-repeat");
                            String title = MessagesBundle.getText("runner.editor.tip.title");
                            Messages.showErrorDialog(msg, title);
                            return;
                        }
                    }

                    deployProfiles.add(new DeployProfile(selectedProfile.toString()));
                    int index = deployProfiles.size() - 1;
                    deployProfileTableModel.fireTableRowsInserted(index, index);
                })
                .setRemoveAction(e -> {
                    int selectedIndex = deployProfileTable.getSelectedRow();
                    if (selectedIndex < 0 || selectedIndex >= deployProfileTable.getRowCount()) {
                        return;
                    }
                    TableUtil.removeSelectedItems(deployProfileTable);
                }).createPanel();
    }

    private void initDeployProfileTable() {
        deployProfileTableModel = new DeployProfileTableModel(this.deployProfiles);
        deployProfileTable = new JBTable(deployProfileTableModel);

        deployProfileTable.getColumnModel().getColumn(0);
        TableColumn activeColumn = deployProfileTable.getColumnModel().getColumn(0);
        activeColumn.setCellRenderer(new BooleanTableCellRenderer());
        activeColumn.setCellEditor(new BooleanTableCellEditor());
        activeColumn.setMinWidth(50);
        activeColumn.setMaxWidth(50);
    }

    private void resetDeployProfileTable(List<DeployProfile> profiles) {
        DefaultTableModel tableModel = (DefaultTableModel) deployProfileTable.getModel();
        if (CollectionUtils.isNotEmpty(profiles)) {
            Object[] row;
            Iterator<DeployProfile> iterator = profiles.iterator();
            while (iterator.hasNext()) {
                DeployProfile deployProfile = iterator.next();

                row = new Object[7];
                int sshId = deployProfile.getSshId();
                int profileId = deployProfile.getProfileId();
                SshServer server = ConfigHelper.getSshServerById(sshId);
                UploadProfile uploadProfile = ConfigHelper.getOneUploadProfileById(sshId, profileId);
                if (uploadProfile == null) {
                    iterator.remove();
                    continue;
                }
                row[0] = deployProfile;
                row[1] = deployProfile.isActive();
                row[2] = server.getIp() + ":" + server.getPort();
                row[3] = uploadProfile.getName();
                row[4] = uploadProfile.getFile();
                row[5] = uploadProfile.getLocation();

                Command cmd;
                Integer cmdId = uploadProfile.getCommandId();
                if (cmdId == null) {
                    cmd = NoneCommand.INSTANCE;
                } else {
                    cmd = ConfigHelper.getCommandById(uploadProfile.getCommandId());
                    if (cmd == null) {
                        cmd = NoneCommand.INSTANCE;
                    }
                }
                row[5] = cmd.getTitle();

                tableModel.addRow(row);
            }
        }
    }

    private void removeSelectedProfile(int idx) {
        deployProfiles.remove(idx);
        DefaultTableModel tableModel = (DefaultTableModel) deployProfileTable.getModel();
        tableModel.removeRow(idx);
    }

}
