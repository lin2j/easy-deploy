package tech.lin2j.idea.plugin.uitl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.remoteServer.agent.util.log.TerminalListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.cloud.CloudTerminalProcess;
import org.jetbrains.plugins.terminal.cloud.CloudTerminalRunner;
import tech.lin2j.idea.plugin.ssh.CustomTtyConnector;
import tech.lin2j.idea.plugin.ssh.CustomTtyConnectorFactory;
import tech.lin2j.idea.plugin.ssh.SshProcess;
import tech.lin2j.idea.plugin.ssh.SshServer;

import java.awt.Dimension;
import java.lang.reflect.Constructor;

/**
 * @author linjinjia
 * @date 2024/3/24 09:00
 */
public class TerminalRunnerUtil {

    private static final Logger log = Logger.getInstance(TerminalRunnerUtil.class);

    private static final Class<?>[] NORMAL_VERSION = {
            Project.class,
            String.class,
            CloudTerminalProcess.class,
            TerminalListener.TtyResizeHandler.class,
            boolean.class
    };

    private static final Class<?>[] __233_VERSION = {
            Project.class,
            String.class,
            CloudTerminalProcess.class,
            TerminalListener.TtyResizeHandler.class,
    };

    /**
     * Retrieve CloudTerminalRunner via reflection
     *
     * @param project project info
     * @param server  ssh server info
     * @return CloudTerminalRunner
     */
    public static CloudTerminalRunner createCloudTerminalRunner(@NotNull Project project,
                                                                    SshServer server) {
        String presentableName = server.getIp() + ":" + server.getPort();
        CustomTtyConnector ttyConnector = CustomTtyConnectorFactory.getCustomTtyConnector(CustomTtyConnector.SSHJ, server);
        ttyConnector.setName(presentableName);
        SshProcess p = new SshProcess(ttyConnector);
        CloudTerminalProcess cloudProcess = new CloudTerminalProcess(p.getOutputStream(), p.getInputStream());
        TerminalListener.TtyResizeHandler handlerBoundLater = (w, h) -> getResizeHandler(ttyConnector).onTtyResizeRequest(w, h);
        return createCloudTerminalRunnerInstance(project, presentableName, cloudProcess, handlerBoundLater);
    }

    private static TerminalListener.TtyResizeHandler getResizeHandler(CustomTtyConnector ttyConnector) {
        return (width, height) -> {
            ttyConnector.resize(new Dimension(width, height));
        };
    }

    private static CloudTerminalRunner createCloudTerminalRunnerInstance(
            @NotNull Project project, String pipeName, CloudTerminalProcess process,
            @Nullable TerminalListener.TtyResizeHandler resizeHandler) {
        CloudTerminalRunner instance = null;
        try {
            Constructor<CloudTerminalRunner> constructor = CloudTerminalRunner.class.getConstructor(NORMAL_VERSION);
            instance = constructor.newInstance(project, pipeName, process, resizeHandler, true);
        } catch (Exception ignored) {
        }
        if (instance == null) {
            try {
                // The constructor of CloudTerminalRunner in version 233 is slightly different
                Constructor<CloudTerminalRunner> constructor = CloudTerminalRunner.class.getConstructor(__233_VERSION);
                instance = constructor.newInstance(project, pipeName, process, resizeHandler);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return instance;
    }
}