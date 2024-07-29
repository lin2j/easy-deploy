package tech.lin2j.idea.plugin.runner;

import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.BooleanTableCellEditor;
import com.intellij.ui.BooleanTableCellRenderer;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.model.DeployProfile;
import tech.lin2j.idea.plugin.ui.dialog.SelectUploadProfileDialog;
import tech.lin2j.idea.plugin.ui.table.DeployProfileTableModel;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.table.TableColumn;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

/**
 * @author linjinjia
 * @date 2024/4/24 21:57
 */
public class DeploySettingsEditor extends SettingsEditor<DeployRunConfiguration> {

    private final JPanel myPanel;
    private final JBTable deployProfileTable = new JBTable();
    private final JBCheckBox parallelExecCheckBox = new JBCheckBox();
    private List<DeployProfile> deployProfiles = new ArrayList<>();
    private Boolean parallelExec = Boolean.FALSE;

    private DeployProfileTableModel deployProfileTableModel;

    public DeploySettingsEditor() {
        refreshDeployProfileTable();

        myPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Deploy profile: ", getDeployProfilePanel(), true)
                .addLabeledComponent("Allow parallel upload: ", parallelExecCheckBox)
                .getPanel();
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return myPanel;
    }

    @Override
    protected void resetEditorFrom(DeployRunConfiguration deployRunConfiguration) {
        this.deployProfiles = deployRunConfiguration.getDeployProfile();
        this.parallelExec = deployRunConfiguration.getParallelExec();
        if (!this.deployProfiles.isEmpty()) {
            refreshDeployProfileTable();
        }
        refreshParallelExecCheckBox();
    }

    @Override
    protected void applyEditorTo(@NotNull DeployRunConfiguration deployRunConfiguration) {
        deployRunConfiguration.setDeployProfiles(deployProfiles);
        deployRunConfiguration.setParallelExec(parallelExec);
    }

    private JPanel getDeployProfilePanel() {
        return ToolbarDecorator.createDecorator(deployProfileTable)
                .setPreferredSize(new Dimension(0, 200))
                .setToolbarPosition(ActionToolbarPosition.TOP)
                .setAddAction(e -> {
                    StringBuilder selectedProfile = new StringBuilder();
                    new SelectUploadProfileDialog(selectedProfile, null).showAndGet();
                    if (StringUtil.isEmpty(selectedProfile)) {
                        return;
                    }
                    DeployProfile newDp = new DeployProfile(selectedProfile.toString());
                    // check repeat
                    boolean anyMatch = this.deployProfiles.stream().anyMatch(dp -> dp.equals(newDp));
                    if (anyMatch) {
                        String msg = MessagesBundle.getText("runner.editor.tip.profile-repeat");
                        String title = MessagesBundle.getText("runner.editor.tip.title");
                        Messages.showErrorDialog(msg, title);
                        return;
                    }
                    deployProfiles.add(newDp);
                    refreshDeployProfileTable();
                })
                .setRemoveAction(e -> {
                    int selectedIndex = deployProfileTable.getSelectedRow();
                    if (selectedIndex < 0 || selectedIndex >= deployProfileTableModel.getRowCount()) {
                        return;
                    }
                    deployProfiles.remove(selectedIndex);
                    refreshDeployProfileTable();
                })
                .disableUpDownActions()
                .createPanel();
    }

    private void refreshDeployProfileTable() {
        deployProfileTableModel = new DeployProfileTableModel(this.deployProfiles);
        deployProfileTable.setModel(deployProfileTableModel);

        deployProfileTable.getColumnModel().getColumn(0);
        TableColumn activeColumn = deployProfileTable.getColumnModel().getColumn(0);
        activeColumn.setCellRenderer(new BooleanTableCellRenderer());
        activeColumn.setCellEditor(new BooleanTableCellEditor());
        activeColumn.setMinWidth(50);
        activeColumn.setMaxWidth(50);
    }

    private void refreshParallelExecCheckBox() {
        parallelExecCheckBox.setSelected(parallelExec);
        parallelExecCheckBox.addActionListener(e -> parallelExec = parallelExecCheckBox.isSelected());
    }

}
