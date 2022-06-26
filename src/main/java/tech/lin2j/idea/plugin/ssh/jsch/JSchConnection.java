package tech.lin2j.idea.plugin.ssh.jsch;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import tech.lin2j.idea.plugin.ssh.SshConnection;
import tech.lin2j.idea.plugin.ssh.SshStatus;

import java.util.Properties;

/**
 * @author linjinjia
 * @date 2022/6/25 11:33
 */
public class JSchConnection implements SshConnection {
    private Session session;

    public JSchConnection(Session session) {
        this.session = session;
    }

    @Override
    public SshStatus upload(String local, String dest) {
        return null;
    }

    @Override
    public void download(String remote, String dest) {

    }

    @Override
    public SshStatus execute(String cmd) {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isClosed() {
        return false;
    }

    public Session getSession() throws JSchException {
        return session;
    }
}