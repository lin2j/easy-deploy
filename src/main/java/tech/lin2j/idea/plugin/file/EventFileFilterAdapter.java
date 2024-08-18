package tech.lin2j.idea.plugin.file;

import com.intellij.openapi.project.Project;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.model.event.CommandExecuteEvent;
import tech.lin2j.idea.plugin.ssh.SshServer;

import javax.swing.SwingUtilities;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * when a file is excluded, publish a {@link CommandExecuteEvent}
 *
 * @author linjinjia
 * @date 2022/12/11 17:50
 */
public class EventFileFilterAdapter implements FileFilter {
    private final AtomicInteger index = new AtomicInteger();

    private final SshServer server;

    private final FileFilter filter;

    private final Project project;

    public EventFileFilterAdapter(Project project, FileFilter fileFilter, SshServer server, String initMsg) {
        if (server == null) {
            throw new IllegalArgumentException("ssh server information should not be null");
        }
        if (fileFilter == null) {
            throw new IllegalArgumentException("file filter should not be null");
        }
        this.filter = fileFilter;
        this.server = server;
        this.project = project;
        invokeUi(initMsg);
    }

    @Override
    public boolean accept(String filename) {
        return filter.accept(filename);
    }

    @Override
    public void accept(String filename, FileAction<Boolean> action) throws Exception {
        boolean accept = filter.accept(filename);
        if (!accept) {
            invokeUi(filename + " exclude\n");
        }
        publishEvent(filename, accept, true);
        action.execute(accept);
        publishEvent(filename, accept, false);
    }

    /**
     * publish an event, comprehensive file name and
     * other conditions to determine the information content
     *
     * @param filename   file name
     * @param accept     whether the file if accepted by filter
     * @param beforeExec whether before filter testing
     */
    private void publishEvent(String filename, boolean accept, boolean beforeExec) {
        if (!accept) {
            return;
        }
        String msg;
        if (beforeExec) {
            // accept and before uploading file
            msg = filename;
        } else {
            // accept and after uploading file
            msg = "Result: [OK]";
        }
        invokeUi(msg);
    }

    private void invokeUi(String executeResult) {
        SwingUtilities.invokeLater(() -> {
            CommandExecuteEvent event = new CommandExecuteEvent(project, server, executeResult, index.get());
            ApplicationContext.getApplicationContext().publishEvent(event);
            index.incrementAndGet();
        });
    }
}