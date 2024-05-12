package tech.lin2j.idea.plugin.runner;

import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.SideBorder;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.FormBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.UploadProfile;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ui.dialog.SelectUploadProfileDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * @author linjinjia
 * @date 2024/4/24 21:57
 */
public class DeploySettingsEditor extends SettingsEditor<DeployRunConfiguration> {

    private final JPanel myPanel;
    private JBTable deployProfileTable;
    private List<String> deployProfiles;

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
        resetDeployProfileTable(deployProfiles);
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
                    String msg = selectedProfile.toString();
                    deployProfiles.add(msg);
                    resetDeployProfileTable(Collections.singletonList(msg));
                })
                .setRemoveAction(e -> {
                    int idx = deployProfileTable.getSelectedRow();
                    removeSelectedProfile(idx);
                }).createPanel();
    }

    private void initDeployProfileTable() {
        DefaultTableModel tableModel = new DefaultTableModel(new Object[0][2], new String[]{"Server", "Profile"});
        deployProfileTable = new JBTable(tableModel);
        deployProfileTable.setModel(tableModel);
    }

    private void resetDeployProfileTable(List<String> profiles) {
        DefaultTableModel tableModel = (DefaultTableModel) deployProfileTable.getModel();
        if (CollectionUtils.isNotEmpty(profiles)) {
            Object[] row;
            for (String deployProfile : profiles) {
                row = new Object[2];
                String[] ss = deployProfile.split("@");
                int sshId = Integer.parseInt(ss[0]);
                int profileId = Integer.parseInt(ss[1]);
                SshServer server = ConfigHelper.getSshServerById(sshId);
                UploadProfile uploadProfile = ConfigHelper.getOneUploadProfileByName(sshId, profileId);
                if (uploadProfile == null) {
                    deployProfiles.remove(deployProfile);
                    continue;
                }
                row[0] = server.getIp() + ":" + server.getPort();
                row[1] = uploadProfile.getName();
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
