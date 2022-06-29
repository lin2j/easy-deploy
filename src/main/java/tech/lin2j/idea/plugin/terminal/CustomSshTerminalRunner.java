package tech.lin2j.idea.plugin.terminal;

import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.project.Project;
import com.jediterm.terminal.TtyConnector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.AbstractTerminalRunner;
import tech.lin2j.idea.plugin.ssh.CustomTtyConnector;
import tech.lin2j.idea.plugin.ssh.CustomTtyConnectorFactory;
import tech.lin2j.idea.plugin.ssh.SshProcess;
import tech.lin2j.idea.plugin.ssh.SshProcessHandler;
import tech.lin2j.idea.plugin.ssh.SshServer;

import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

/**
 * @author linjinjia
 * @date 2022/6/24 21:05
 */
public class CustomSshTerminalRunner extends AbstractTerminalRunner<SshProcess> {

    private final String title;

    private final CustomTtyConnector ttyConnector;

    private final SshProcess sshProcess;

    private final ProcessTtyConnectorAdapter processTtyConnectorAdapter;

    public CustomSshTerminalRunner(@NotNull Project project,
                                   SshServer server,
                                   Charset charset) {
        super(project);
        this.title = server.getIp() + ":" + server.getPort();
        this.ttyConnector = CustomTtyConnectorFactory.getCustomTtyConnector(CustomTtyConnector.JSCH, server);
        this.ttyConnector.setName(title);
        this.sshProcess = new SshProcess(ttyConnector);
        this.processTtyConnectorAdapter = new ProcessTtyConnectorAdapter(sshProcess, ttyConnector, charset);
    }

    @Override
    public SshProcess createProcess(@Nullable String directory) throws ExecutionException {
        return sshProcess;
    }

    @Override
    protected ProcessHandler createProcessHandler(SshProcess process) {
        return new SshProcessHandler(ttyConnector);
    }

    @Override
    protected String getTerminalConnectionName(SshProcess process) {
        return title;
    }

    @Override
    protected TtyConnector createTtyConnector(SshProcess process) {
        return processTtyConnectorAdapter;
    }

    @Override
    public String runningTargetName() {
        return title;
    }

    public boolean isTerminalSessionPersistent() {
        return false;
    }
}