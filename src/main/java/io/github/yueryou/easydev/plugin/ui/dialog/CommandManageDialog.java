package io.github.yueryou.easydev.plugin.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBTabbedPane;
import org.jetbrains.annotations.Nullable;
import io.github.yueryou.easydev.plugin.ui.component.CommandManagePanel;
import io.github.yueryou.easydev.plugin.ui.component.CommandPipelinePanel;
import io.github.yueryou.easydev.plugin.ui.component.CommandSettingPanel;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.*;
import java.awt.*;

public class CommandManageDialog extends DialogWrapper {
    private final Project project;
    private final JPanel root = new JPanel(new BorderLayout());

    private final CommandManagePanel commandManagePanel;
    private final CommandPipelinePanel commandPipelinePanel;
    private final CommandSettingPanel commandSettingsPanel;
    private JBTabbedPane tabs;

    public CommandManageDialog(@Nullable Project project) {
        super(project);
        this.project = project;
        // 快捷指令管理
        commandManagePanel = new CommandManagePanel(project);
        // 指令、任务编排
        commandPipelinePanel = new CommandPipelinePanel(project);
        // 指令设置
        commandSettingsPanel = new CommandSettingPanel(project);

        setTitle(MessagesBundle.getText("dialog.panel.command.title"));
        setSize(500, 0);
        init();
    }

    /**
     * 点击 OK 按钮时执行对应标签页的操作
     */
    @Override
    protected void doOKAction() {
        // 根据当前选中的标签页执行对应的操作
        if (tabs == null) {
            super.doOKAction();
            return;
        }

        int selectedIndex = tabs.getSelectedIndex();
        if (selectedIndex == 0) {
            // 快捷命令标签页 - 执行发送命令
            commandManagePanel.executeCommand();
        } else if (selectedIndex == 1) {
            // 任务编排标签页 - 执行选中的流水线
            commandPipelinePanel.executeSelectedPipeline();
        }
        // 设置标签页没有执行操作，直接关闭
        super.doOKAction();
    }

    /**
     * 覆盖 OK 按钮文本，显示为"运行"（中文）或"Run"（英文）
     */
    protected String getOKButtonText() {
        return MessagesBundle.getText("pipeline.run");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        String mangeTab = MessagesBundle.getText("dialog.command.tab.manage");
        String pipelineTab = MessagesBundle.getText("dialog.command.tab.task");
        String settingTab = MessagesBundle.getText("dialog.command.tab.setting");

        tabs = new JBTabbedPane();
        tabs.addTab(mangeTab, commandManagePanel.createUI());
        tabs.addTab(pipelineTab, commandPipelinePanel.createUI());
        tabs.addTab(settingTab, commandSettingsPanel.createUI());

        root.add(tabs);
        ApplicationContext.getApplicationContext().addApplicationListener(commandManagePanel);
        return tabs;
    }
}
