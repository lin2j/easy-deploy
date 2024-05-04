package tech.lin2j.idea.plugin.file;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import tech.lin2j.idea.plugin.domain.model.event.CommandExecuteEvent;
import tech.lin2j.idea.plugin.ssh.SshServer;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * when a file is excluded, publish a {@link CommandExecuteEvent}
 *
 * @author linjinjia
 * @date 2022/12/11 17:50
 */
@NotThreadSafe
public class ConsoleFileFilterAdapter implements FileFilter {
    private final AtomicInteger index = new AtomicInteger();

    private final SshServer server;

    private final FileFilter filter;

    private final ConsoleView console;

    public ConsoleFileFilterAdapter(ConsoleView console, FileFilter fileFilter, SshServer server, String initMsg) {
        if (server == null) {
            throw new IllegalArgumentException("ssh server information should not be null");
        }
        if (fileFilter == null) {
            throw new IllegalArgumentException("file filter should not be null");
        }
        this.filter = fileFilter;
        this.server = server;
        this.console = console;
        print(initMsg);
    }

    @Override
    public boolean accept(String filename) {
        return filter.accept(filename);
    }

    @Override
    public void accept(String filename, FileAction<Boolean> action) throws Exception {
        boolean accept = filter.accept(filename);
        if (!accept) {
            print(filename + " exclude\n");
        }
        consoleOutput(filename, accept, true);
        action.execute(accept);
        consoleOutput(filename, accept, false);
    }

    /**
     * Output information to the console.
     *
     * @param filename   file name
     * @param accept     whether the file if accepted by filter
     * @param beforeExec whether before filter testing
     */
    private void consoleOutput(String filename, boolean accept, boolean beforeExec) {
        if (!accept) {
            return;
        }
        String msg;
        if (beforeExec) {
            // accept and before uploading file
            msg = filename + "\n";
        } else {
            // accept and after uploading file
            msg = "Result: [OK]\n";
        }
        print(msg);
    }

    private void print(String executeResult) {
        console.print(executeResult, ConsoleViewContentType.NORMAL_OUTPUT);
    }
}