package tech.lin2j.idea.plugin.uitl;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.domain.model.Command;
import tech.lin2j.idea.plugin.domain.model.event.CommandExecuteEvent;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.service.SshService;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.SshStatus;

import static com.intellij.openapi.ui.DialogWrapper.OK_EXIT_CODE;

/**
 * @author linjinjia
 * @date 2022/5/7 08:58
 */
public class CommandUtil {
    private static final SshService sshService = SshService.getInstance();

    public static void executeAndShowMessages(Project project, Command cmd, SshServer server, DialogWrapper dialogWrapper) {
        String title = String.format("executing command on %s:%s", server.getIp(), server.getPort());
        ToolWindow messages = ToolWindowManager.getInstance(project).getToolWindow("Messages");
        messages.setTitle(title);
        messages.activate(null);

        ProgressManager.getInstance().run(new Task.Backgroundable(project, title) {
            final CommandExecuteEvent event = new CommandExecuteEvent(cmd, null);

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(false);
                try {
                    // it is not recommended to executing command like "tail -f", because it will block the thread
                    SshStatus status = sshService.execute(server, cmd.generateCmdLine());
                    event.setSuccess(status.isSuccess());
                    event.setExecResult(status.getMessage());
                } catch (Exception e1) {
                    event.setSuccess(false);
                    event.setExecResult(e1.getMessage());
                }
                indicator.setFraction(1);
            }

            @Override
            public void onFinished() {
                dialogWrapper.close(OK_EXIT_CODE);
                if (!event.isSuccess()) {
                    Messages.showErrorDialog("Fail to execute command: " + event.getExecResult(), "Error");
                    return;
                }
                ApplicationContext.getApplicationContext().publishEvent(event);
            }
        });
    }
}