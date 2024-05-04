package tech.lin2j.idea.plugin.ssh.sshj;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.xfer.TransferListener;
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
    private final SFTPClient sftpClient;

    public SshjConnection(SSHClient sshClient) throws IOException {
        this.sshClient = sshClient;
        this.sftpClient = sshClient.newSFTPClient();
    }

    public void setTransferListener(TransferListener transferListener) {
        if (transferListener == null) {
            return;
        }
        sftpClient.getFileTransfer().setTransferListener(transferListener);
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
            FileAttributes attr = sftpClient.stat(dest);
            if (attr == null) {
                // remote directory not exist
                mkdirs(dest);
            }
            sftpClient.put(local, dest);
        } else {
            throw new FileNotFoundException("local file not found: " + local);
        }

    }

    @Override
    public void download(String remote, String dest) throws IOException {
        sftpClient.get(remote, dest);
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
    public void mkdirs(String dir) throws IOException {
        sftpClient.mkdirs(dir);
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