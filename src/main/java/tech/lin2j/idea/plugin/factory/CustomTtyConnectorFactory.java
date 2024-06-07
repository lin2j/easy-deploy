package tech.lin2j.idea.plugin.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.lin2j.idea.plugin.ssh.CustomTtyConnector;
import tech.lin2j.idea.plugin.ssh.SshConnectionManager;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.exception.RemoteSdkException;
import tech.lin2j.idea.plugin.ssh.sshj.SshjConnection;
import tech.lin2j.idea.plugin.ssh.sshj.SshjTtyConnector;

import java.io.IOException;

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
    public static CustomTtyConnector getCustomTtyConnector(String type, SshServer server) throws RemoteSdkException {
        if (CustomTtyConnector.SSHJ.equals(type)) {
            try {
                SshjConnection connection = SshConnectionManager.makeSshjConnection(server);
                return new SshjTtyConnector(connection);
            } catch (IOException e) {
                LOG.error("Error connecting server: " + e.getMessage());
                throw new RemoteSdkException("Error connecting server: " + e.getMessage(), e);
            }
        }
        throw new IllegalArgumentException(type);
    }
}