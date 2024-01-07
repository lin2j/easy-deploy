package tech.lin2j.idea.plugin.ssh.jsch;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import tech.lin2j.idea.plugin.ssh.SshConnection;
import tech.lin2j.idea.plugin.ssh.SshStatus;


/**
 * @author linjinjia
 * @date 2022/6/25 11:33
 */
public class JschConnection implements SshConnection {
    private final Session session;

    public JschConnection(Session session) {
        this.session = session;
    }

    @Override
    public boolean isConnected() {
        return session.isConnected();
    }

    @Override
    public void upload(String local, String dest) {
    }

    @Override
    public void download(String remote, String dest) {

    }

    @Override
    public String execute(String cmd) {
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