package tech.lin2j.idea.plugin.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.jediterm.terminal.Questioner;
import tech.lin2j.idea.plugin.ssh.jsch.JSchConnection;

import java.util.Properties;

/**
 * @author linjinjia
 * @date 2022/6/25 15:34
 */
public class SshConnectionManager {

    public static JSchConnection makeJSchConnection(SshServer server) throws JSchException{
        JSch jSch = new JSch();
        Session session = jSch.getSession(server.getUsername(), server.getIp(), server.getPort());
        session.setPassword(server.getPassword());

        Properties config = new Properties();
        config.put("compression.s2c", "zlib,none");
        config.put("compression.c2s", "zlib,none");
        config.put("StrictHostKeyChecking", "no");
        config.put("PreferredAuthentications", "publickey,keyboard-interactive,password");
        session.setConfig(config);
        session.setUserInfo(new MyUserInfo());
        session.setTimeout(5000);
        session.connect();
        session.setTimeout(0);

        return new JSchConnection(session);
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
            return false;
        }

        @Override
        public void showMessage(String message) {
        }
    }
}