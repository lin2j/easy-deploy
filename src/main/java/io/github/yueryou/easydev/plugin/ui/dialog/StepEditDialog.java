package io.github.yueryou.easydev.plugin.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import io.github.yueryou.easydev.plugin.model.LocalCommandStep;
import io.github.yueryou.easydev.plugin.model.PipelineStep;
import io.github.yueryou.easydev.plugin.model.RemoteCommandStep;
import io.github.yueryou.easydev.plugin.model.StepType;
import io.github.yueryou.easydev.plugin.model.UploadStep;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.model.Command;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.UploadProfile;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;
import tech.lin2j.idea.plugin.uitl.UiUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 步骤编辑对话框
 */
public class StepEditDialog extends DialogWrapper {

    private final Project project;
    private final PipelineStep existingStep;

    private JComboBox<StepType> typeComboBox;
    private JTextField nameField;

    // Local command fields
    private JTextField commandField;
    private JTextField localWorkingDirField;
    private JTextField timeoutField;
    private JComboBox<String> localCommandComboBox;

    // Upload fields
    private JComboBox<String> uploadProfileComboBox;
    private JComboBox<String> uploadServerComboBox;
    private JCheckBox createRemoteDirCheckBox;

    // Remote command fields
    private JTextField remoteCommandField;
    private JTextField remoteWorkingDirField;
    private JComboBox<String> remoteCommandComboBox;
    private JComboBox<String> remoteServerComboBox;

    private JPanel cardsPanel;
    private CardLayout cardLayout;

    private PipelineStep stepResult;

    public StepEditDialog(Project project, PipelineStep existingStep) {
        super(project);
        this.project = project;
        this.existingStep = existingStep;

        setTitle(existingStep != null ? MessagesBundle.getText("pipeline.step.edit.title") : MessagesBundle.getText("pipeline.step.add.title"));

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        nameField = new JTextField(30);

        typeComboBox = new JComboBox<>(StepType.values());
        typeComboBox.addActionListener(e -> onTypeChanged());

        // 本地命令组件
        commandField = new JTextField(30);
        localWorkingDirField = new JTextField(30);
        timeoutField = new JTextField("0", 10);

        // 上传组件
        List<String> profileItems = new ArrayList<>();
        profileItems.add("");
        for (UploadProfile profile : ConfigHelper.getAllUploadProfiles()) {
            profileItems.add(profile.getId() + " - " + profile.getName());
        }
        uploadProfileComboBox = new JComboBox<>(profileItems.toArray(new String[0]));
        createRemoteDirCheckBox = new JCheckBox(MessagesBundle.getText("pipeline.step.upload.create.dir"), true);

        // Server 选择组件
        List<String> serverItems = new ArrayList<>();
        serverItems.add("");
        for (SshServer server : ConfigHelper.sshServers()) {
            serverItems.add(server.getId() + " - " + server.getIp() + ":" + server.getPort());
        }

        // 远程命令组件
        remoteCommandField = new JTextField(30);
        remoteWorkingDirField = new JTextField(30);

        // 命令选择下拉框（复用已有命令）
        List<String> commandItems = new ArrayList<>();
        commandItems.add("");
        for (Command cmd : ConfigHelper.getAllCommands()) {
            commandItems.add(cmd.getId() + " - " + cmd.getTitle());
        }
        localCommandComboBox = new JComboBox<>(commandItems.toArray(new String[0]));
        remoteCommandComboBox = new JComboBox<>(commandItems.toArray(new String[0]));

        // Server 选择下拉框
        uploadServerComboBox = new JComboBox<>(serverItems.toArray(new String[0]));
        remoteServerComboBox = new JComboBox<>(serverItems.toArray(new String[0]));

        // 卡片面板
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);

        cardsPanel.add(createLocalCommandPanel(), "LOCAL_COMMAND");
        cardsPanel.add(createUploadPanel(), "UPLOAD");
        cardsPanel.add(createRemoteCommandPanel(), "REMOTE_COMMAND");

        // 如果是编辑模式，填充数据
        if (existingStep != null) {
            nameField.setText(existingStep.getName());
            typeComboBox.setSelectedItem(existingStep.getType());
            populateFieldsFromStep(existingStep);
        }

        onTypeChanged();

        return FormBuilder.createFormBuilder()
                .addLabeledComponent(MessagesBundle.getText("pipeline.step.type"), typeComboBox)
                .addLabeledComponent(MessagesBundle.getText("pipeline.step.name"), nameField)
                .addLabeledComponent(MessagesBundle.getText("pipeline.step.config"), cardsPanel, true)
                .getPanel();
    }

    private JPanel createLocalCommandPanel() {
        return FormBuilder.createFormBuilder()
                .addLabeledComponent(MessagesBundle.getText("pipeline.step.local.command"), commandField)
                .addLabeledComponent(MessagesBundle.getText("pipeline.step.local.working.dir"), localWorkingDirField)
                .addLabeledComponent(MessagesBundle.getText("pipeline.step.local.timeout"), timeoutField)
                .addLabeledComponent(MessagesBundle.getText("pipeline.step.local.use.command"), localCommandComboBox)
                .getPanel();
    }

    private JPanel createUploadPanel() {
        return FormBuilder.createFormBuilder()
                .addLabeledComponent(MessagesBundle.getText("pipeline.step.server"), uploadServerComboBox)
                .addLabeledComponent(MessagesBundle.getText("pipeline.step.upload.profile"), uploadProfileComboBox)
                .addComponent(createRemoteDirCheckBox)
                .getPanel();
    }

    private JPanel createRemoteCommandPanel() {
        return FormBuilder.createFormBuilder()
                .addLabeledComponent(MessagesBundle.getText("pipeline.step.server"), remoteServerComboBox)
                .addLabeledComponent(MessagesBundle.getText("pipeline.step.remote.command"), remoteCommandField)
                .addLabeledComponent(MessagesBundle.getText("pipeline.step.remote.working.dir"), remoteWorkingDirField)
                .addLabeledComponent(MessagesBundle.getText("pipeline.step.remote.use.command"), remoteCommandComboBox)
                .getPanel();
    }

    private void onTypeChanged() {
        StepType selectedType = (StepType) typeComboBox.getSelectedItem();
        if (selectedType != null) {
            cardLayout.show(cardsPanel, selectedType.name());
        }
    }

    private void populateFieldsFromStep(PipelineStep step) {
        switch (step.getType()) {
            case LOCAL_COMMAND:
                LocalCommandStep localStep = (LocalCommandStep) step;
                commandField.setText(localStep.getCommand());
                localWorkingDirField.setText(localStep.getWorkingDir());
                timeoutField.setText(String.valueOf(localStep.getTimeout()));
                if (localStep.getCommandId() != null) {
                    selectItemInComboBox(localCommandComboBox, localStep.getCommandId());
                }
                break;
            case UPLOAD:
                UploadStep uploadStep = (UploadStep) step;
                selectItemInComboBox(uploadServerComboBox, uploadStep.getServerId());
                selectItemInComboBox(uploadProfileComboBox, uploadStep.getUploadProfileId());
                createRemoteDirCheckBox.setSelected(uploadStep.isCreateRemoteDir());
                break;
            case REMOTE_COMMAND:
                RemoteCommandStep remoteStep = (RemoteCommandStep) step;
                selectItemInComboBox(remoteServerComboBox, remoteStep.getServerId());
                remoteCommandField.setText(remoteStep.getCommand());
                remoteWorkingDirField.setText(remoteStep.getWorkingDir());
                if (remoteStep.getCommandId() != null) {
                    selectItemInComboBox(remoteCommandComboBox, remoteStep.getCommandId());
                }
                break;
        }
    }

    private void selectItemInComboBox(JComboBox<String> comboBox, String id) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            String item = comboBox.getItemAt(i);
            if (item != null && item.startsWith(id + " - ")) {
                comboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    @Override
    protected void doOKAction() {
        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(
                    getContentPane(),
                    MessagesBundle.getText("pipeline.step.validation.error.name"),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        StepType type = (StepType) typeComboBox.getSelectedItem();
        PipelineStep step = createStepFromFields(type);
        if (step == null) {
            return;
        }
        step.setName(name.trim());

        // 将步骤存储在对话框中供获取
        this.stepResult = step;

        super.doOKAction();
    }

    private PipelineStep createStepFromFields(StepType type) {
        switch (type) {
            case LOCAL_COMMAND:
                LocalCommandStep localStep = new LocalCommandStep();
                localStep.setCommand(commandField.getText());
                localStep.setWorkingDir(localWorkingDirField.getText());

                String timeoutText = timeoutField.getText().trim();
                int timeout = 0;
                if (!timeoutText.isEmpty()) {
                    try {
                        timeout = Integer.parseInt(timeoutText);
                        if (timeout < 0) {
                            javax.swing.JOptionPane.showMessageDialog(
                                    getContentPane(),
                                    "超时时间不能为负数",
                                    "验证失败",
                                    javax.swing.JOptionPane.WARNING_MESSAGE
                            );
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        javax.swing.JOptionPane.showMessageDialog(
                                getContentPane(),
                                "超时时间必须是有效数字",
                                "验证失败",
                                javax.swing.JOptionPane.WARNING_MESSAGE
                        );
                        return null;
                    }
                }
                localStep.setTimeout(timeout);

                // 检查是否选择了已有命令
                String selectedCommand = (String) localCommandComboBox.getSelectedItem();
                String commandId = UiUtil.extractIdFromComboBoxItem(selectedCommand);
                if (commandId != null) {
                    localStep.setCommandId(commandId);
                }

                return localStep;

            case UPLOAD:
                UploadStep uploadStep = new UploadStep();
                String selectedProfile = (String) uploadProfileComboBox.getSelectedItem();
                String profileId = UiUtil.extractIdFromComboBoxItem(selectedProfile);
                if (profileId != null) {
                    uploadStep.setUploadProfileId(profileId);
                }
                // 设置 ServerId
                String selectedServer = (String) uploadServerComboBox.getSelectedItem();
                String serverId = UiUtil.extractIdFromComboBoxItem(selectedServer);
                uploadStep.setServerId(serverId);

                uploadStep.setCreateRemoteDir(createRemoteDirCheckBox.isSelected());
                return uploadStep;

            case REMOTE_COMMAND:
                RemoteCommandStep remoteStep = new RemoteCommandStep();
                remoteStep.setCommand(remoteCommandField.getText());
                remoteStep.setWorkingDir(remoteWorkingDirField.getText());

                // 设置 ServerId
                String selectedRemoteServer = (String) remoteServerComboBox.getSelectedItem();
                String remoteServerId = UiUtil.extractIdFromComboBoxItem(selectedRemoteServer);
                remoteStep.setServerId(remoteServerId);

                // 检查是否选择了已有命令
                String selectedRemoteCommand = (String) remoteCommandComboBox.getSelectedItem();
                String remoteCommandId = UiUtil.extractIdFromComboBoxItem(selectedRemoteCommand);
                if (remoteCommandId != null) {
                    remoteStep.setCommandId(remoteCommandId);
                }

                return remoteStep;

            default:
                throw new IllegalArgumentException("Unknown step type: " + type);
        }
    }

    public PipelineStep getStep() {
        return stepResult;
    }
}