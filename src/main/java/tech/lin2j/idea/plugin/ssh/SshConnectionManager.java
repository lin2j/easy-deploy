package tech.lin2j.idea.plugin.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import tech.lin2j.idea.plugin.enums.AuthType;
import tech.lin2j.idea.plugin.ssh.jsch.JschConnection;
import tech.lin2j.idea.plugin.uitl.FileUtil;

import javax.swing.JOptionPane;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author linjinjia
 * @date 2022/6/25 15:34
 */
public class SshConnectionManager {

    public static JschConnection makeJschConnection(SshServer server) throws JSchException{
        return new JschConnection(makeJschSession(server));
    }

    public static Session makeJschSession(SshServer server) throws JSchException {
        JSch jsch = new JSch();
        String home = String.valueOf(System.getProperty("user.home"));
        String knownHostsFileName = Paths.get(home, ".ssh", "known_hosts").toString();
        jsch.setKnownHosts(knownHostsFileName);

        boolean needPemPrivateKey = AuthType.needPemPrivateKey(server.getAuthType());
        if (needPemPrivateKey) {
            String pemPrvKey = FileUtil.replaceHomeSymbol(server.getPemPrivateKey());
            jsch.addIdentity(pemPrvKey);
        }

        Session session = jsch.getSession(server.getUsername(), server.getIp(), server.getPort());
        session.setPassword(server.getPassword());

        Properties config = new Properties();
        config.put("compression.s2c", "zlib,none");
        config.put("compression.c2s", "zlib,none");
        config.put("StrictHostKeyChecking", "ask");
        config.put("PreferredAuthentications", "publickey,keyboard-interactive,password");
        session.setConfig(config);
        session.setUserInfo(new MyUserInfo());
        session.setTimeout(5000);
        session.connect();
        session.setTimeout(0);

        return session;
    }

    private static class MyUserInfo implements UserInfo {
        private MyUserInfo() {
        }

        @Override
        public String getPassphrase() {
            return null;
        }

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public boolean promptPassword(String message) {
            return false;
        }

        @Override
        public boolean promptPassphrase(String message) {
            return false;
        }

        @Override
        public boolean promptYesNo(String message) {
            Object[] options = {"yes", "no"};
            int foo = JOptionPane.showOptionDialog(null,
                    message,
                    "Warning",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return foo == 0;
        }

        @Override
        public void showMessage(String message) {
            JOptionPane.showMessageDialog(null, message);
        }
    }
}