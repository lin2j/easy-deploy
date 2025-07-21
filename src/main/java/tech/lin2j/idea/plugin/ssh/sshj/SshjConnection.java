package tech.lin2j.idea.plugin.ssh.sshj;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPException;
import net.schmizz.sshj.xfer.TransferListener;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.ssh.CommandLog;
import tech.lin2j.idea.plugin.ssh.SshConnection;
import tech.lin2j.idea.plugin.ssh.SshStatus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Deque;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author linjinjia
 * @date 2024/1/5 21:19
 */
public class SshjConnection implements SshConnection {

    private static final Logger log = Logger.getInstance(SshjConnection.class);

    private final Deque<SSHClient> clients;
    private final SSHClient sshClient;
    private final SFTPClient sftpClient;
    private SCPFileTransfer scpFileTransfer;

    public SshjConnection(Deque<SSHClient> clients) throws IOException {
        this.clients = clients;
        this.sshClient = clients.getLast();
        this.sftpClient = sshClient.newSFTPClient();
        if (ConfigHelper.isSCPTransferMode()) {
            scpFileTransfer = sshClient.newSCPFileTransfer();
        }
    }

    public void setTransferListener(TransferListener transferListener) {
        if (transferListener == null) {
            return;
        }
        sftpClient.getFileTransfer().setTransferListener(transferListener);
        if (ConfigHelper.isSCPTransferMode()) {
            scpFileTransfer.setTransferListener(transferListener);
        }
    }

    public SSHClient getSshClient() {
        return sshClient;
    }

    @Override
    public boolean isConnected() {
        return sshClient.isConnected();
    }

    @Override
    public void upload(String local, String dest) throws IOException {
        if (ConfigHelper.isSCPTransferMode()) {
            scpUpload(local, dest);
            return;
        }
        log.debug("Upload file from local to remote directory");
        File localFile = new File(local);
        if (localFile.exists() && localFile.canRead()) {
            FileAttributes attr = null;
            try {
                attr = sftpClient.stat(dest);
            } catch (SFTPException ignored) {
                log.debug("no such remote file: " + dest);
            }
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
        if (ConfigHelper.isSCPTransferMode()) {
            scpDownload(remote, dest);
            return;
        }
        sftpClient.get(remote, dest);
    }

    @Override
    public void scpUpload(String local, String dest) throws IOException {
        File localFile = new File(local);
        if (localFile.exists() && localFile.canRead()) {
            SshStatus checkFileExist = execute("ls " + dest);
            if (!checkFileExist.isSuccess()) {
                if (checkFileExist.getMessage().contains("No such file")) {
                    execute("mkdir -p " + dest);
                }
            }
            scpFileTransfer.upload(local, dest);
        } else {
            throw new FileNotFoundException("local file not found: " + local);
        }
    }

    @Override
    public void scpDownload(String remote, String dest) throws IOException {
        scpFileTransfer.download(remote, dest);
    }

    @Override
    public SshStatus execute(String cmd) throws IOException {
        Session session = this.sshClient.startSession();
        try {
            Session.Command command = session.exec(cmd);

            String result = IOUtils.readFully(command.getInputStream()).toString();
            String err = IOUtils.readFully(command.getErrorStream()).toString();
            command.close();

            boolean isOk = command.getExitStatus() == 0;
            String msg = isOk ? result : err;
            return new SshStatus(isOk, msg);
        } finally {
            close(session);
        }
    }

    @Override
    public SshStatus execute(String cmd, CommandLog commandLog) throws IOException {
        Session session = this.sshClient.startSession();
        try {
            Session.Command command = session.exec(cmd);

            String result = IOUtils.readFully(command.getInputStream()).toString();
            String err = IOUtils.readFully(command.getErrorStream()).toString();
            command.close();

            // Output command result to console
            if (!result.isEmpty()) {
                commandLog.println(result.trim());
            }
            if (!err.isEmpty()) {
                commandLog.println(err.trim());
            }

            boolean isOk = command.getExitStatus() == 0;
            String msg = isOk ? result : err;
            return new SshStatus(isOk, msg);
        } finally {
            close(session);
        }
    }

    @Override
    public FutureTask<Void> executeAsync(CommandLog commandLog, String cmd,
                                         AtomicBoolean cancel, boolean closeAfterFinished) {
        FutureTask<Void> task = new FutureTask<>(() -> {
            Session session = this.sshClient.startSession();
            try {
                Session.Command command = session.exec(cmd);
                InputStream std = command.getInputStream();
                InputStream err = command.getErrorStream();
                for (; ; ) {
                    if (cancel.get()) {
                        break;
                    }
                    BufferedReader stdReader = new BufferedReader(new InputStreamReader(std));
                    BufferedReader errReader = new BufferedReader(new InputStreamReader(err));

                    String msg;
                    while ((msg = errReader.readLine()) != null) {
                        commandLog.println(msg);
                    }

                    while ((msg = stdReader.readLine()) != null) {
                        commandLog.println(msg);
                    }

                    if (session.isOpen()) {
                        TimeUnit.MILLISECONDS.sleep(50);
                        continue;
                    }

                    if (!(std.available() > 0 || err.available() > 0)) {
                        break;
                    }
                }
            } finally {
                close(session);
                if (closeAfterFinished) {
                    this.close();
                }
                printFinished(commandLog);
            }
            return null;
        });
        ApplicationManager.getApplication().executeOnPooledThread(task);
        return task;
    }

    @Override
    public void mkdirs(String dir) throws IOException {
        sftpClient.mkdirs(dir);
    }

    @Override
    public void close() {
        if (clients != null) {
            log.info("Close ssh connection, size: " + clients.size());
            while (!clients.isEmpty()) {
                try {
                    clients.removeLast().close();
                } catch (Exception ignored) {

                }
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

    private static void printFinished(CommandLog commandLog) {
        commandLog.info("Finished at: " + LocalDateTime.now());
    }
}