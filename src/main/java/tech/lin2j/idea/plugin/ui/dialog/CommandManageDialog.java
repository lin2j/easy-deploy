package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBTabbedPane;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.ui.component.CommandManagePanel;
import tech.lin2j.idea.plugin.ui.component.CommandPipelinePanel;
import tech.lin2j.idea.plugin.ui.component.CommandSettingPanel;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.*;
import java.awt.*;

public class CommandManageDialog extends DialogWrapper {
    private final Project project;
    private final JPanel root = new JPanel(new BorderLayout());

    private final CommandManagePanel commandManagePanel;
    private final CommandPipelinePanel commandPipelinePanel;
    private final CommandSettingPanel commandSettingsPanel;

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

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        String mangeTab = MessagesBundle.getText("dialog.command.tab.manage");
        String pipelineTab = MessagesBundle.getText("dialog.command.tab.task");
        String settingTab = MessagesBundle.getText("dialog.command.tab.setting");

        JBTabbedPane tabs = new JBTabbedPane();
        tabs.addTab(mangeTab, commandManagePanel.createUI());
        tabs.addTab(pipelineTab, commandPipelinePanel.createUI());
        tabs.addTab(settingTab, commandSettingsPanel.createUI());

        root.add(tabs);

        return tabs;
    }
}
