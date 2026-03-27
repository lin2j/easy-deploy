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
    private JCheckBox createRemoteDirCheckBox;

    // Remote command fields
    private JTextField remoteCommandField;
    private JTextField remoteWorkingDirField;
    private JComboBox<String> remoteCommandComboBox;

    private JPanel cardsPanel;
    private CardLayout cardLayout;

    private PipelineStep stepResult;

    public StepEditDialog(Project project, PipelineStep existingStep) {
        super(project);
        this.project = project;
        this.existingStep = existingStep;

        setTitle(existingStep != null ? "编辑步骤" : "添加步骤");

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
        createRemoteDirCheckBox = new JCheckBox("创建远程目录", true);

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
                .addLabeledComponent("步骤类型", typeComboBox)
                .addLabeledComponent("步骤名称", nameField)
                .addLabeledComponent("配置", cardsPanel, true)
                .getPanel();
    }

    private JPanel createLocalCommandPanel() {
        return FormBuilder.createFormBuilder()
                .addLabeledComponent("命令", commandField)
                .addLabeledComponent("工作目录", localWorkingDirField)
                .addLabeledComponent("超时时间 (秒，0=不限制)", timeoutField)
                .addLabeledComponent("或使用已有命令", localCommandComboBox)
                .getPanel();
    }

    private JPanel createUploadPanel() {
        return FormBuilder.createFormBuilder()
                .addLabeledComponent("上传配置", uploadProfileComboBox)
                .addComponent(createRemoteDirCheckBox)
                .getPanel();
    }

    private JPanel createRemoteCommandPanel() {
        return FormBuilder.createFormBuilder()
                .addLabeledComponent("命令", remoteCommandField)
                .addLabeledComponent("工作目录", remoteWorkingDirField)
                .addLabeledComponent("或使用已有命令", remoteCommandComboBox)
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
                selectItemInComboBox(uploadProfileComboBox, uploadStep.getUploadProfileId());
                createRemoteDirCheckBox.setSelected(uploadStep.isCreateRemoteDir());
                break;
            case REMOTE_COMMAND:
                RemoteCommandStep remoteStep = (RemoteCommandStep) step;
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
                    "请输入步骤名称",
                    "验证失败",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        StepType type = (StepType) typeComboBox.getSelectedItem();
        PipelineStep step = createStepFromFields(type);
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

                String timeoutText = timeoutField.getText();
                try {
                    int timeout = Integer.parseInt(timeoutText);
                    localStep.setTimeout(timeout > 0 ? timeout : 0);
                } catch (NumberFormatException e) {
                    localStep.setTimeout(0);
                }

                // 检查是否选择了已有命令
                String selectedCommand = (String) localCommandComboBox.getSelectedItem();
                if (selectedCommand != null && !selectedCommand.isEmpty()) {
                    String[] parts = selectedCommand.split(" - ");
                    if (parts.length > 0) {
                        localStep.setCommandId(parts[0]);
                    }
                }

                return localStep;

            case UPLOAD:
                UploadStep uploadStep = new UploadStep();
                String selectedProfile = (String) uploadProfileComboBox.getSelectedItem();
                if (selectedProfile != null && !selectedProfile.isEmpty()) {
                    String[] parts = selectedProfile.split(" - ");
                    if (parts.length > 0) {
                        uploadStep.setUploadProfileId(parts[0]);
                    }
                }
                uploadStep.setCreateRemoteDir(createRemoteDirCheckBox.isSelected());
                return uploadStep;

            case REMOTE_COMMAND:
                RemoteCommandStep remoteStep = new RemoteCommandStep();
                remoteStep.setCommand(remoteCommandField.getText());
                remoteStep.setWorkingDir(remoteWorkingDirField.getText());

                // 检查是否选择了已有命令
                String selectedRemoteCommand = (String) remoteCommandComboBox.getSelectedItem();
                if (selectedRemoteCommand != null && !selectedRemoteCommand.isEmpty()) {
                    String[] parts = selectedRemoteCommand.split(" - ");
                    if (parts.length > 0) {
                        remoteStep.setCommandId(parts[0]);
                    }
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
