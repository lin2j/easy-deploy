package tech.lin2j.idea.plugin.ssh;

import com.intellij.openapi.ui.Messages;
import com.jcraft.jsch.JSchException;
import com.jediterm.terminal.Questioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.lin2j.idea.plugin.ssh.exception.RemoteSdkException;
import tech.lin2j.idea.plugin.ssh.jsch.JSchTtyConnector;

import javax.annotation.Nullable;

/**
 * @author linjinjia
 * @date 2022/6/25 11:29
 */
public class CustomTtyConnectorFactory {

    public static final Logger LOG = LoggerFactory.getLogger(CustomTtyConnectorFactory.class);

    /**
     * return a tty connector
     * @param type implementation of ssh2, {@link CustomTtyConnector}
     * @param server server information
     * @return tty connector
     */
    public static @Nullable CustomTtyConnector getCustomTtyConnector(String type, SshServer server) throws RemoteSdkException {
        switch (type) {
            case CustomTtyConnector.JSCH:
                try {
                    SshConnection sshConnection = SshConnectionManager.makeJSchConnection(server);
                    return new JSchTtyConnector(sshConnection);
                } catch (JSchException e) {
                    LOG.error("Error connecting server: " + e.getMessage());
                    throw new RemoteSdkException("Error connecting server: " + e.getMessage(), e);
                }
            default:
                throw new IllegalArgumentException(type);
        }
    }
}