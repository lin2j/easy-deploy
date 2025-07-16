package tech.lin2j.idea.plugin.action;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.DumbAwareAction;
import icons.MyIcons;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ssh.CommandLog;
import tech.lin2j.idea.plugin.uitl.WebBrowseUtil;

/**
 * @author linjinjia
 * @date 2024/4/25 22:43
 */
public class StopConsoleTaskAction extends NewUpdateThreadAction {
    private final CommandLog commandLog;

    public StopConsoleTaskAction(CommandLog commandLog) {
        super("Stop", "Stop all tasks", AllIcons.Actions.Suspend);
        this.commandLog = commandLog;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean enabled = commandLog.taskNum() > 0;
        e.getPresentation().setEnabled(enabled);
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent e) {
        commandLog.stopAllTasks();
    }
}