package tech.lin2j.idea.plugin.ssh.jsch;

import com.jcraft.jsch.Session;
import org.junit.Test;
import tech.lin2j.idea.plugin.ssh.SshConnectionManager;
import tech.lin2j.idea.plugin.ssh.SshServer;

/**
 * @author linjinjia
 * @date 2023/12/30 00:42
 */
public class SCPClientTest {

    @Test
    public void testSendFile() {
        try {
            SshServer server = new SshServer();
            server.setIp("10.211.55.4");
            server.setUsername("root");
            server.setPort(22);
            server.setPassword("123");
            server.setAuthType(1);

            Session session = SshConnectionManager.makeJschSession(server);

            SCPClient scpClient = new SCPClient(session);
            scpClient.put("/Users/kenny/Downloads/github-recovery-codes.txt", "/root/test");

            session.disconnect();
            System.out.println("disconnect");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}