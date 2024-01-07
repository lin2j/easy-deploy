package tech.lin2j.idea.plugin.ssh.sshj;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;
import tech.lin2j.idea.plugin.ssh.SshConnection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author linjinjia
 * @date 2024/1/5 21:19
 */
public class SshjConnection implements SshConnection {

    private static final Logger log = Logger.getInstance(SshjConnection.class);

    private final SSHClient sshClient;

    public SshjConnection(SSHClient sshClient) {
        this.sshClient = sshClient;
    }

    @Override
    public boolean isConnected() {
        return sshClient.isConnected();
    }

    @Override
    public void upload(String local, String dest) throws IOException {
        log.debug("Upload file from local to remote directory");
        File localFile = new File(local);
        if (localFile.exists() && localFile.canRead()) {
            if (!this.execute("stat " + dest).contains("File")) {
                // remote directory not exist
                String mkdirResult = this.execute("mkdir -p " + dest);
                if (StringUtil.isNotEmpty(mkdirResult)) {
                    // Command execution error
                    throw new RuntimeException(mkdirResult);
                }
            }
            SCPFileTransfer scpFileTransfer = this.sshClient.newSCPFileTransfer();
            scpFileTransfer.upload(local, dest);
        } else {
            throw new FileNotFoundException("local file not found: " + local);
        }

    }

    @Override
    public void download(String remote, String dest) throws IOException {
        SCPFileTransfer scpFileTransfer = this.sshClient.newSCPFileTransfer();
        scpFileTransfer.download(remote, dest);
    }

    @Override
    public String execute(String cmd) throws IOException {
        Session session = this.sshClient.startSession();
        try {
            Session.Command command = session.exec(cmd);

            String result = IOUtils.readFully(command.getInputStream()).toString();
            String err = IOUtils.readFully(command.getErrorStream()).toString();
            command.close();

            return command.getExitStatus() == 0 ? result : err;
        } finally {

            close(session);
        }
    }

    @Override
    public void close() {
        if (sshClient != null && sshClient.isConnected()) {
            try {
                sshClient.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public boolean isClosed() {
        return sshClient == null || !sshClient.isConnected();
    }

    private void close(Session session) {
        if (session != null) {
            try {
                session.close();
            } catch (Exception ignored) {
            }
        }
    }
}