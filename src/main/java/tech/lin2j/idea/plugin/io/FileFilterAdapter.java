package tech.lin2j.idea.plugin.io;

import tech.lin2j.idea.plugin.domain.model.event.CommandExecuteEvent;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.ssh.SshServer;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * when a file is excluded, publish a {@link CommandExecuteEvent}
 *
 * @author linjinjia
 * @date 2022/12/11 17:50
 */
@NotThreadSafe
public class FileFilterAdapter implements FileFilter {
    private final AtomicInteger index = new AtomicInteger();

    private final SshServer server;

    private final FileFilter filter;

    public FileFilterAdapter(FileFilter fileFilter, SshServer server, String initMsg) {
        if (server == null) {
            throw new IllegalArgumentException("ssh server information should not be null");
        }
        if (fileFilter == null) {
            throw new IllegalArgumentException("file filter should not be null");
        }
        this.filter = fileFilter;
        this.server = server;
        invokeUi(initMsg);
    }

    @Override
    public boolean accept(String filename) {
        return filter.accept(filename);
    }

    @Override
    public void accept(String filename, FileAction<Boolean> action) throws IOException {
        boolean result = filter.accept(filename);
        publishEvent(filename, result, true);
        action.execute(result);
        publishEvent(filename, result, false);
    }

    /**
     * publish an event, comprehensive file name and
     * other conditions to determine the information content
     *
     * @param filename file name
     * @param accept   whether the file if accepted by filter
     * @param before   whether before filter testing
     */
    private void publishEvent(String filename, boolean accept,
                              boolean before) {
        String msg;
        if (accept && before) {
            // accept and before uploading file
            msg = filename;
        } else if (accept) {
            // accept and after uploading file
            msg = String.format("%15s\n", "[OK]");
        } else {
            // not accept
            msg = filename + " exclude";
        }
        invokeUi(msg);
    }

    private void invokeUi(String executeResult) {
        SwingUtilities.invokeLater(() -> {
            CommandExecuteEvent event = new CommandExecuteEvent(server, executeResult, index.get());
            ApplicationContext.getApplicationContext().publishEvent(event);
            index.incrementAndGet();
        });
    }
}