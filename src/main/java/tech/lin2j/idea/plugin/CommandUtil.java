package tech.lin2j.idea.plugin;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import tech.lin2j.idea.plugin.domain.model.Command;
import tech.lin2j.idea.plugin.domain.model.SshServer;
import tech.lin2j.idea.plugin.domain.model.SshStatus;
import tech.lin2j.idea.plugin.domain.model.event.CommandExecuteEvent;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.service.SshService;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.intellij.openapi.ui.DialogWrapper.OK_EXIT_CODE;

/**
 * @author linjinjia
 * @date 2022/5/7 08:58
 */
public class CommandUtil {

    private static final Logger LOG = Logger.getInstance(CommandUtil.class);

    private static SshService sshService = SshService.getInstance();

    private static final BlockingQueue<CommandExecuteEvent> EVENT_QUEUE = new LinkedBlockingQueue<>(100);

    public static void executeAndShowMessages(Command cmd, SshServer server, DialogWrapper dialogWrapper) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            CommandExecuteEvent event = new CommandExecuteEvent(cmd, null);
            try {
                // it is not recommended to executing command like "tail -f", because it will block the thread
                SshStatus status = sshService.execute(server, cmd.generateCmdLine());
                event.setExecResult(status.getMessage());
            } catch (Exception e1) {
                event.setExecResult(e1.getMessage());
            }
            EVENT_QUEUE.offer(event);
        });
        dialogWrapper.close(OK_EXIT_CODE);
        try {
            // avoid the execute command thread block the ui thread
            CommandExecuteEvent event = EVENT_QUEUE.poll(10, TimeUnit.SECONDS);
            if (event == null) {
                event = new CommandExecuteEvent(cmd, "timeout for execute");
            }
            DataContext dataContext = DataManager.getInstance().getDataContext();
            Project project = dataContext.getData(DataKey.create("project"));
            ToolWindow messages = ToolWindowManager.getInstance(project).getToolWindow("Messages");
            String target = server.getIp() + ":" + server.getPort();
            messages.setTitle("execute command on " + target);
            messages.activate(null);
            ApplicationContext.getApplicationContext().publishEvent(event);
        } catch (InterruptedException ex) {
            if (LOG.isDebugEnabled()) {
                LOG.error("execute command fail: " + ex.getMessage(), ex);
            }
        }
    }
}