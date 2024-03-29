package tech.lin2j.idea.plugin.ssh;

import com.jcraft.jsch.JSchException;
import net.schmizz.sshj.SSHClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.lin2j.idea.plugin.ssh.exception.RemoteSdkException;
import tech.lin2j.idea.plugin.ssh.jsch.JSchTtyConnector;
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
        switch (type) {
            case CustomTtyConnector.JSCH:
                try {
                    SshConnection sshConnection = SshConnectionManager.makeJschConnection(server);
                    return new JSchTtyConnector(sshConnection);
                } catch (JSchException e) {
                    LOG.error("Error connecting server: " + e.getMessage());
                    throw new RemoteSdkException("Error connecting server: " + e.getMessage(), e);
                }
            case CustomTtyConnector.SSHJ:
                try {
                    SSHClient sshClient = SshConnectionManager.makeSshClient(server);
                    return new SshjTtyConnector(sshClient);
                } catch (IOException e) {
                    LOG.error("Error connecting server: " + e.getMessage());
                    throw new RemoteSdkException("Error connecting server: " + e.getMessage(), e);
                }
            default:
                throw new IllegalArgumentException(type);
        }
    }
}