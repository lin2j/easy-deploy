package tech.lin2j.idea.plugin.terminal;

import org.jetbrains.plugins.terminal.cloud.CloudTerminalProcess;
import tech.lin2j.idea.plugin.ssh.CustomTtyConnector;

/**
 * @author linjinjia
 * @date 2024/5/16 23:40
 */
public class ClosableCloudTerminalProcess extends CloudTerminalProcess {

    private final CustomTtyConnector connector;

    public ClosableCloudTerminalProcess(CustomTtyConnector connector) {
        super(connector.getOutputStream(), connector.getInputStream());
        this.connector = connector;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (connector != null) {
            connector.close();
        }
    }
}