package io.github.yueryou.easydev.plugin.ui.component;

import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import io.github.yueryou.easydev.plugin.model.Pipeline;
import io.github.yueryou.easydev.plugin.model.PipelineConfigPersistence;
import io.github.yueryou.easydev.plugin.ui.dialog.PipelineEditDialog;
import io.github.yueryou.easydev.plugin.ui.render.PipelineListCellRenderer;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.service.impl.PluginNotificationService;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;
import tech.lin2j.idea.plugin.uitl.UiUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.github.yueryou.easydev.plugin.executor.PipelineExecutor;
import io.github.yueryou.easydev.plugin.model.PipelineResult;

/**
 * 自定义任务流水线面板
 */
public class CommandPipelinePanel extends JPanel {

    private final Project project;
    private final JPanel root;

    /**
     * 搜索输入框
     */
    private JBTextField searchInput;

    /**
     * 流水线列表
     */
    private JBList<Pipeline> pipelineList;

    private final PluginNotificationService notificationService;

    public CommandPipelinePanel(Project project) {
        this.project = project;
        this.notificationService = ApplicationManager.getApplication().getService(PluginNotificationService.class);

        initInput();
        initPipelineList();
        bindInputChangeListener(loadPipelineList());

        root = FormBuilder.createFormBuilder()
                .addLabeledComponent(MessagesBundle.getText("pipeline.search"), searchInput)
                .addComponentFillVertically(createPipelineToolbarPanel(), 8)
                .getPanel();
        root.setPreferredSize(new Dimension(UiUtil.screenWidth() / 2, 600));
    }

    private void initInput() {
        searchInput = new JBTextField();
        searchInput.getEmptyText().setText(MessagesBundle.getText("pipeline.search.placeholder"));
    }

    private void initPipelineList() {
        pipelineList = new JBList<>();
        pipelineList.setCellRenderer(new PipelineListCellRenderer());
    }

    private JPanel createPipelineToolbarPanel() {
        return ToolbarDecorator.createDecorator(pipelineList)
                .setToolbarPosition(ActionToolbarPosition.TOP)
                .disableUpDownActions()
                .setAddAction(e -> showPipelineEditDialog(new Pipeline()))
                .setEditAction(e -> {
                    Pipeline pipeline = pipelineList.getSelectedValue();
                    if (pipeline != null) {
                        showPipelineEditDialog(pipeline);
                    }
                })
                .setRemoveAction(e -> {
                    Pipeline pipeline = pipelineList.getSelectedValue();
                    if (pipeline == null) {
                        return;
                    }
                    boolean confirm = UiUtil.deleteConfirm(pipeline.getName());
                    if (confirm) {
                        PipelineConfigPersistence.removePipeline(pipeline);
                        loadPipelineList();
                    }
                })
                .addExtraAction(new RunPipelineAction())
                .createPanel();
    }

    private void showPipelineEditDialog(Pipeline pipeline) {
        PipelineEditDialog dialog = new PipelineEditDialog(project, pipeline);
        if (dialog.showAndGet()) {
            loadPipelineList();
        }
    }

    private void bindInputChangeListener(List<Pipeline> pipelines) {
        searchInput.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                String text = searchInput.getText();
                if (text.isBlank()) {
                    pipelineList.setListData(pipelines.toArray(new Pipeline[0]));
                } else {
                    List<Pipeline> searchList = pipelines.stream()
                            .filter(pipeline -> pipeline.getName().contains(text))
                            .toList();
                    pipelineList.setListData(searchList.toArray(new Pipeline[0]));
                }
            }
        });
    }

    public List<Pipeline> loadPipelineList() {
        List<Pipeline> pipelines = PipelineConfigPersistence.getAllPipelines();
        pipelineList.setListData(pipelines.toArray(new Pipeline[0]));
        return pipelines;
    }

    public JPanel createUI() {
        return root;
    }

    /**
     * 运行流水线操作
     */
    private class RunPipelineAction extends AnAction {
        public RunPipelineAction() {
            super(MessagesBundle.getText("pipeline.run"), null, com.intellij.icons.AllIcons.Actions.Execute);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            executePipeline();
        }
    }

    private void executePipeline() {
        executePipelineFromStep(0);
    }

    private void executePipelineFromStep(int startIndex) {
        Pipeline pipeline = pipelineList.getSelectedValue();
        if (pipeline == null) {
            notificationService.showNotification(project, MessagesBundle.getText("pipeline.notification.title.running"),
                MessagesBundle.getText("pipeline.error.no.selected"));
            return;
        }

        if (pipeline.getServerId() == null) {
            notificationService.showNotification(project, MessagesBundle.getText("pipeline.notification.title.running"),
                MessagesBundle.getText("pipeline.error.no.server"));
            return;
        }

        if (pipeline.getSteps() == null || pipeline.getSteps().isEmpty()) {
            notificationService.showNotification(project, MessagesBundle.getText("pipeline.notification.title.running"),
                MessagesBundle.getText("pipeline.error.no.steps"));
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(project, MessagesBundle.getText("pipeline.running") + pipeline.getName()) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    SshServer server = ConfigHelper.getSshServerById(Integer.parseInt(pipeline.getServerId()));

                    if (server == null) {
                        notificationService.showNotification(project, MessagesBundle.getText("pipeline.notification.title.running"),
                            MessagesBundle.getText("pipeline.notification.server.not.found"));
                        return;
                    }

                    Consumer<String> logConsumer = message -> {};

                    PipelineResult result = PipelineExecutor.executeFromStep(pipeline, server, project, logConsumer, startIndex);

                    ApplicationManager.getApplication().invokeLater(() -> {
                        String title = result.isSuccess()
                            ? MessagesBundle.getText("pipeline.notification.success.title")
                            : MessagesBundle.getText("pipeline.notification.failure.title");

                        StringBuilder message = new StringBuilder();
                        if (!result.isSuccess() && result.getFailedStep() != null) {
                            message.append(MessagesBundle.getText("pipeline.notification.failure.step"))
                                   .append(result.getFailedStep().getName());
                        } else {
                            message.append(pipeline.getName());
                        }

                        notificationService.showNotification(project, title, message.toString());
                    });

                } catch (NumberFormatException e) {
                    notificationService.showNotification(project, MessagesBundle.getText("pipeline.notification.title.running"),
                        MessagesBundle.getText("pipeline.notification.server.id.error") + e.getMessage());
                } catch (Exception e) {
                    notificationService.showNotification(project, MessagesBundle.getText("pipeline.notification.title.running"),
                        MessagesBundle.getText("pipeline.notification.execution.error") + e.getMessage());
                }
            }
        });
    }
}
