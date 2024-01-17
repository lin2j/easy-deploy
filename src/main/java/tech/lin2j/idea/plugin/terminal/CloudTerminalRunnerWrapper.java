package tech.lin2j.idea.plugin.terminal;

import com.intellij.openapi.project.Project;
import com.intellij.remoteServer.agent.util.log.TerminalListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.terminal.cloud.CloudTerminalProcess;
import org.jetbrains.plugins.terminal.cloud.CloudTerminalRunner;
import tech.lin2j.idea.plugin.ssh.CustomTtyConnector;
import tech.lin2j.idea.plugin.ssh.CustomTtyConnectorFactory;
import tech.lin2j.idea.plugin.ssh.SshProcess;
import tech.lin2j.idea.plugin.ssh.SshServer;

import java.awt.Dimension;


/**
 * @author linjinjia
 * @date 2024/1/17 22:54
 */
public class CloudTerminalRunnerWrapper {
    private CustomTtyConnector ttyConnector;

    private final TerminalListener.TtyResizeHandler myResizeHandler = (width, height) -> {
        ttyConnector.resize(new Dimension(width, height));
    };

    private final CloudTerminalRunner cloudTerminalRunner;

    public CloudTerminalRunnerWrapper(@NotNull Project project,
                                      SshServer server) {
        String presentableName = server.getIp() + ":" + server.getPort();
        ttyConnector = CustomTtyConnectorFactory.getCustomTtyConnector(CustomTtyConnector.SSHJ, server);
        ttyConnector.setName(presentableName);
        SshProcess p = new SshProcess(ttyConnector);
        CloudTerminalProcess cloudProcess = new CloudTerminalProcess(p.getOutputStream(), p.getInputStream());
        TerminalListener.TtyResizeHandler handlerBoundLater = (w, h) -> getResizeHandler().onTtyResizeRequest(w, h);
        this.cloudTerminalRunner = new CloudTerminalRunner(project, presentableName, cloudProcess, handlerBoundLater, true);

    }

    private TerminalListener.TtyResizeHandler getResizeHandler() {
        return myResizeHandler;
    }

    public CloudTerminalRunner getCloudTerminalRunner() {
        return cloudTerminalRunner;
    }
}