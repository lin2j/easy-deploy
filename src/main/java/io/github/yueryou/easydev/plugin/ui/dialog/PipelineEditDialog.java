package io.github.yueryou.easydev.plugin.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.FormBuilder;
import io.github.yueryou.easydev.plugin.model.FailureStrategy;
import io.github.yueryou.easydev.plugin.model.Pipeline;
import io.github.yueryou.easydev.plugin.model.PipelineConfigPersistence;
import io.github.yueryou.easydev.plugin.model.PipelineStep;
import io.github.yueryou.easydev.plugin.ui.render.PipelineStepListCellRenderer;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 流水线编辑对话框
 */
public class PipelineEditDialog extends DialogWrapper {

    private final Project project;
    private final Pipeline pipeline;

    private JTextField nameField;
    private JComboBox<String> serverComboBox;
    private JComboBox<FailureStrategy> failureStrategyComboBox;
    private JBList<PipelineStep> stepList;
    private DefaultListModel<PipelineStep> stepListModel;

    public PipelineEditDialog(Project project, Pipeline pipeline) {
        super(project);
        this.project = project;
        this.pipeline = pipeline;

        setTitle(pipeline.getId() != null ? MessagesBundle.getText("pipeline.edit.title") : MessagesBundle.getText("pipeline.edit.new.title"));

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        nameField = new JTextField(30);
        if (pipeline.getName() != null) {
            nameField.setText(pipeline.getName());
        }

        // 服务器选择
        List<String> serverItems = new ArrayList<>();
        List<SshServer> servers = ConfigHelper.sshServers();
        serverItems.add("");
        java.util.Map<Integer, SshServer> serverMap = new java.util.HashMap<>();
        for (SshServer server : servers) {
            String item = server.getId() + " - " + server.getIp() + ":" + server.getPort();
            serverItems.add(item);
            serverMap.put(server.getId(), server);
        }
        serverComboBox = new JComboBox<>(serverItems.toArray(new String[0]));
        if (pipeline.getServerId() != null) {
            SshServer selectedServer = serverMap.get(Integer.parseInt(pipeline.getServerId()));
            if (selectedServer != null) {
                serverComboBox.setSelectedItem(selectedServer.getId() + " - " + selectedServer.getIp() + ":" + selectedServer.getPort());
            }
        }

        // 失败策略选择
        failureStrategyComboBox = new JComboBox<>(FailureStrategy.values());
        if (pipeline.getOnFailure() != null) {
            failureStrategyComboBox.setSelectedItem(pipeline.getOnFailure());
        }

        // 步骤列表
        stepListModel = new DefaultListModel<>();
        if (pipeline.getSteps() != null) {
            for (PipelineStep step : pipeline.getSteps()) {
                stepListModel.addElement(step);
            }
        }
        stepList = new JBList<>(stepListModel);
        stepList.setCellRenderer(new PipelineStepListCellRenderer());

        JPanel stepToolbarPanel = ToolbarDecorator.createDecorator(stepList)
                .setAddAction(e -> addStep())
                .setEditAction(e -> editStep())
                .setRemoveAction(e -> removeStep())
                .setMoveUpAction(e -> moveStepUp())
                .setMoveDownAction(e -> moveStepDown())
                .createPanel();

        return FormBuilder.createFormBuilder()
                .addLabeledComponent(MessagesBundle.getText("pipeline.edit.name"), nameField)
                .addLabeledComponent(MessagesBundle.getText("pipeline.edit.server"), serverComboBox)
                .addLabeledComponent(MessagesBundle.getText("pipeline.edit.failure.strategy"), failureStrategyComboBox)
                .addLabeledComponent(MessagesBundle.getText("pipeline.edit.steps"), stepToolbarPanel, true)
                .getPanel();
    }

    @Override
    protected void doOKAction() {
        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            Messages.showErrorDialog(MessagesBundle.getText("pipeline.step.validation.error.name"), "Error");
            return;
        }

        String serverSelectedItem = (String) serverComboBox.getSelectedItem();
        String serverId = null;
        if (serverSelectedItem != null && !serverSelectedItem.isEmpty()) {
            String[] parts = serverSelectedItem.split(" - ");
            if (parts.length > 0) {
                serverId = parts[0];
            }
        }

        if (serverId == null) {
            Messages.showErrorDialog(MessagesBundle.getText("pipeline.error.no.server"), "Error");
            return;
        }

        FailureStrategy failureStrategy = (FailureStrategy) failureStrategyComboBox.getSelectedItem();

        // 保存配置
        pipeline.setName(name.trim());
        pipeline.setServerId(serverId);
        pipeline.setOnFailure(failureStrategy);

        // 收集步骤
        List<PipelineStep> steps = new ArrayList<>();
        for (int i = 0; i < stepListModel.size(); i++) {
            steps.add(stepListModel.getElementAt(i));
        }
        pipeline.setSteps(steps);

        if (pipeline.getId() == null) {
            PipelineConfigPersistence.addPipeline(pipeline);
        } else {
            PipelineConfigPersistence.updatePipeline(pipeline);
        }

        super.doOKAction();
    }

    private void addStep() {
        StepEditDialog dialog = new StepEditDialog(project, null);
        if (dialog.showAndGet()) {
            PipelineStep step = dialog.getStep();
            stepListModel.addElement(step);
        }
    }

    private void editStep() {
        PipelineStep selectedStep = stepList.getSelectedValue();
        if (selectedStep == null) {
            return;
        }

        StepEditDialog dialog = new StepEditDialog(project, selectedStep);
        if (dialog.showAndGet()) {
            int index = stepList.getSelectedIndex();
            stepListModel.set(index, dialog.getStep());
        }
    }

    private void removeStep() {
        PipelineStep selectedStep = stepList.getSelectedValue();
        if (selectedStep == null) {
            return;
        }

        int confirm = Messages.showYesNoDialog(
                MessagesBundle.getText("pipeline.action.remove") + " \"" + selectedStep.getName() + "\"?",
                MessagesBundle.getText("pipeline.action.remove"),
                Messages.getQuestionIcon()
        );

        if (confirm == Messages.YES) {
            stepListModel.removeElement(selectedStep);
        }
    }

    private void moveStepUp() {
        int index = stepList.getSelectedIndex();
        if (index > 0) {
            PipelineStep step = stepListModel.remove(index);
            stepListModel.add(index - 1, step);
            stepList.setSelectedIndex(index - 1);
        }
    }

    private void moveStepDown() {
        int index = stepList.getSelectedIndex();
        if (index >= 0 && index < stepListModel.size() - 1) {
            PipelineStep step = stepListModel.remove(index);
            stepListModel.add(index + 1, step);
            stepList.setSelectedIndex(index + 1);
        }
    }
}
