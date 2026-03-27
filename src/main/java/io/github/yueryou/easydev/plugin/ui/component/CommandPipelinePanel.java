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
import io.github.yueryou.easydev.plugin.ui.render.PipelineListCellRenderer;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.service.impl.PluginNotificationService;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;
import tech.lin2j.idea.plugin.uitl.UiUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.List;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import tech.lin2j.idea.plugin.model.ConfigHelper;

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

    /**
     * 已选择的流水线
     */
    private transient Pipeline selectedPipeline;

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
        pipelineList.addListSelectionListener(e -> {
            Pipeline pipeline = pipelineList.getSelectedValue();
            if (pipeline != null) {
                selectedPipeline = pipeline;
            } else {
                selectedPipeline = null;
            }
        });
    }

    private JPanel createPipelineToolbarPanel() {
        return ToolbarDecorator.createDecorator(pipelineList)
                .setToolbarPosition(ActionToolbarPosition.TOP)
                .disableUpDownActions()
                .setAddAction(e -> {
                    // TODO: 移动到 Task 6 实现
                    notificationService.showNotification(project, "提示", "PipelineEditDialog 开发中...");
                })
                .setEditAction(e -> {
                    // TODO: 移动到 Task 6 实现
                    notificationService.showNotification(project, "提示", "PipelineEditDialog 开发中...");
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
        if (selectedPipeline == null) {
            notificationService.showNotification(project, "运行流水线", MessagesBundle.getText("pipeline.error.no.selected"));
            return;
        }

        if (selectedPipeline.getServerId() == null) {
            notificationService.showNotification(project, "运行流水线", MessagesBundle.getText("pipeline.error.no.server"));
            return;
        }

        if (selectedPipeline.getSteps() == null || selectedPipeline.getSteps().isEmpty()) {
            notificationService.showNotification(project, "运行流水线", MessagesBundle.getText("pipeline.error.no.steps"));
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(project, MessagesBundle.getText("pipeline.running") + selectedPipeline.getName()) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    // 获取服务器配置
                    tech.lin2j.idea.plugin.ssh.SshServer server =
                        ConfigHelper.getSshServerById(Integer.parseInt(selectedPipeline.getServerId()));

                    if (server == null) {
                        notificationService.showNotification(project, "运行流水线", "未找到服务器配置");
                        return;
                    }

                    // 构建日志输出
                    StringBuilder logOutput = new StringBuilder();
                    java.util.function.Consumer<String> logConsumer = logOutput::append;

                    // 执行流水线
                    io.github.yueryou.easydev.plugin.model.PipelineResult result =
                        io.github.yueryou.easydev.plugin.executor.PipelineExecutor.executeFromStep(
                            selectedPipeline,
                            server,
                            project,
                            logConsumer,
                            startIndex
                        );

                    // 显示结果
                    ApplicationManager.getApplication().invokeLater(() -> {
                        if (result.isSuccess()) {
                            notificationService.showNotification(
                                project,
                                "流水线执行成功",
                                selectedPipeline.getName()
                            );
                        } else {
                            notificationService.showNotification(
                                project,
                                "流水线执行失败",
                                "失败步骤：" + (result.getFailedStep() != null ? result.getFailedStep().getName() : "未知")
                            );
                        }
                    });

                } catch (NumberFormatException e) {
                    notificationService.showNotification(project, "运行流水线", "服务器 ID 格式错误：" + e.getMessage());
                } catch (Exception e) {
                    notificationService.showNotification(project, "运行流水线", "执行异常：" + e.getMessage());
                }
            }
        });
    }
}
