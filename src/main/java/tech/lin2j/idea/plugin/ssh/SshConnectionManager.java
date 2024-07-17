package tech.lin2j.idea.plugin.ssh;

import com.intellij.openapi.diagnostic.Logger;
import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.DirectConnection;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.PluginSetting;
import tech.lin2j.idea.plugin.enums.AuthType;
import tech.lin2j.idea.plugin.ssh.exception.RemoteSdkException;
import tech.lin2j.idea.plugin.ssh.sshj.SshjConnection;
import tech.lin2j.idea.plugin.ssh.sshj.SshjLoggerFactory;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;

/**
 * @author linjinjia
 * @date 2022/6/25 15:34
 */
public class SshConnectionManager {

    private static final PluginSetting setting = ConfigHelper.pluginSetting();

    private static final Logger log = Logger.getInstance(SshConnectionManager.class);

    public static Deque<SSHClient> makeSshClients(SshServer server) throws RemoteSdkException, IOException {
        LinkedList<SshServer> hostChain = new LinkedList<>();
        hostChain.add(server);
        SshServer tmp = server;
        boolean proxyNotFound = false;
        while (tmp.getProxy() != null) {
            SshServer proxy = ConfigHelper.getSshServerById(tmp.getProxy());
            if (proxy == null) {
                log.error("jump server not found: " + tmp.getProxy());
                proxyNotFound = true;
                break;
            }

            if (hostChain.contains(proxy)) {
                String err = MessagesBundle.getText("ssh.connect.proxy.cycle");
                throw new RemoteSdkException(err);
            }
            hostChain.addFirst(proxy);
            tmp = proxy;
        }
        if (proxyNotFound) {
            throw new RemoteSdkException("Proxy host not found");
        }

        DefaultConfig defaultConfig = new DefaultConfig();
        defaultConfig.setLoggerFactory(new SshjLoggerFactory(log));
        defaultConfig.setKeepAliveProvider(KeepAliveProvider.HEARTBEAT);

        Deque<SSHClient> clients = new LinkedList<>();
        SshServer errHost = null;
        try {
            for(SshServer host : hostChain) {
                errHost = host;
                SSHClient client = new SSHClient(defaultConfig);
                client.addHostKeyVerifier(new PromiscuousVerifier());
                client.setConnectTimeout(5000);
                if (setting.isSshKeepalive()) {
                    client.getConnection().getKeepAlive().setKeepAliveInterval(setting.getHeartbeatInterval());
                }
                // jump
                if (clients.size() == 0) {
                    client.connect(host.getIp(), host.getPort());
                } else {
                    DirectConnection tunnel = clients.getLast().newDirectConnection(host.getIp(), host.getPort());
                    client.connectVia(tunnel);
                }
                // auth
                boolean needPemPrivateKey = AuthType.needPemPrivateKey(host.getAuthType());
                if (needPemPrivateKey) {
                    KeyProvider keyProvider = client.loadKeys(host.getPemPrivateKey());
                    client.authPublickey(host.getUsername(), keyProvider);
                } else {
                    client.authPassword(host.getUsername(), host.getPassword());
                }

                clients.addLast(client);
            }
        } catch (Exception e) {
            String errMsg = e.getMessage();
            if (errHost != null) {
                errMsg += ": " + errHost.getIp();
            }
            throw new RemoteSdkException(errMsg);
        }

        return clients;
    }

    public static SshjConnection makeSshjConnection(SshServer server) throws RemoteSdkException, IOException {
        return new SshjConnection(makeSshClients(server));
    }
}