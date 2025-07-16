package tech.lin2j.idea.plugin.service.impl;

import net.schmizz.sshj.xfer.TransferListener;
import tech.lin2j.idea.plugin.file.filter.FileFilter;
import tech.lin2j.idea.plugin.service.ISshService;
import tech.lin2j.idea.plugin.ssh.CommandLog;
import tech.lin2j.idea.plugin.ssh.SshConnectionManager;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.SshStatus;
import tech.lin2j.idea.plugin.ssh.sshj.SshjConnection;

import java.io.File;
import java.util.concurrent.FutureTask;

/**
 * @author linjinjia
 * @date 2024/1/5 21:14
 */
public class SshjSshService implements ISshService {

    @Override
    public SshStatus isValid(SshServer sshServer) {
        String msg = "OK";
        boolean status = false;
        SshjConnection sshjConnection = null;
        try {
            sshjConnection = SshConnectionManager.makeSshjConnection(sshServer);
            if (sshjConnection.isConnected()) {
                status = true;
            }
        } catch (Exception e) {
            msg = e.getMessage();
        } finally {
            close(sshjConnection);
        }
        return new SshStatus(status, msg);
    }

    @Override
    public SshStatus execute(SshServer sshServer, String command) {
        SshjConnection sshjConnection = null;
        try {
            sshjConnection = SshConnectionManager.makeSshjConnection(sshServer);
            return sshjConnection.execute(command);
        } catch (Exception e) {
            return new SshStatus(false, e.getMessage());
        } finally {
            close(sshjConnection);
        }
    }

    @Override
    public void executeAsync(CommandLog commandLog, SshServer sshServer, String command) {
        SshjConnection sshjConnection = null;
        try {
            sshjConnection = SshConnectionManager.makeSshjConnection(sshServer);
            FutureTask<Void> task = sshjConnection.executeAsync(commandLog, command, true);
            commandLog.addTask(task);
        } catch (Exception e) {
            commandLog.error(e.getMessage());
        }
    }

    @Override
    public void executeAsync(CommandLog commandLog, SshjConnection connection, String command) {
        FutureTask<Void> task = connection.executeAsync(commandLog, command, true);
        commandLog.addTask(task);
    }

    @Override
    public SshStatus download(SshServer sshServer, String remoteFile, String localFile) {
        String msg = "OK";
        boolean status = false;
        SshjConnection sshjConnection = null;
        try {
            sshjConnection = SshConnectionManager.makeSshjConnection(sshServer);
            sshjConnection.download(remoteFile, localFile);
            status = true;
        } catch (Exception e) {
            msg = e.getMessage();
        } finally {
            close(sshjConnection);
        }
        return new SshStatus(status, msg);
    }

    @Override
    public SshStatus upload(FileFilter filter, SshServer sshServer,
                            String localFile, String remoteDir, TransferListener listener) {
        SshStatus status = new SshStatus(true, "OK");

        SshjConnection sshjConnection = null;
        try {
            sshjConnection = SshConnectionManager.makeSshjConnection(sshServer);
            sshjConnection.setTransferListener(listener);

            File file = new File(localFile);
            if (file.isDirectory()) {
                remoteDir = remoteDir + "/" + file.getName();
                sshjConnection.mkdirs(remoteDir);
            }

            if (new File(localFile).isDirectory()) {
                putDir(sshjConnection, filter, localFile, remoteDir);
            } else {
                putFile(sshjConnection, filter, localFile, remoteDir);
            }
        } catch (Exception e) {
            status.setSuccess(false);
            status.setMessage(e.getMessage());
        } finally {
            close(sshjConnection);
        }
        return status;
    }

    @Override
    public boolean upload(FileFilter filter, SshjConnection sshjConnection,
                          String localFile, String remoteDir,
                          CommandLog commandLog, boolean createRemoteDir) {
        try {
            File file = new File(localFile);
            if (file.isDirectory() && createRemoteDir) {
                remoteDir = remoteDir + "/" + file.getName();
                sshjConnection.mkdirs(remoteDir);
            }
            if (file.isDirectory()) {
                putDir(sshjConnection, filter, localFile, remoteDir);
            } else {
                putFile(sshjConnection, filter, localFile, remoteDir);
            }
        } catch (Exception e) {
            commandLog.error(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public SshStatus isDirExist(SshServer server, String remoteTargetDir) {
        return null;
    }

    /**
     * Upload files or folders to the specified directory on the
     * remote server, during which filter will be called for file
     * filtering. When encountering a subdirectory, this method
     * will be recursively called.
     *
     * @param connection   SSH connection
     * @param filter       File filters, only accepted ones will be sent
     * @param localFile    File or directory on local machine
     * @param remoteDstDir Target directory on the remote server
     * @throws Exception Exception
     */
    private void putDir(SshjConnection connection, FileFilter filter,
                        String localFile, String remoteDstDir) throws Exception {
        File dir = new File(localFile);
        if (dir.isDirectory()) {
            String[] fileList = dir.list();
            if (fileList == null) {
                return;
            }
            for (String f : fileList) {
                String localFullFileName = localFile + "/" + f;
                if (new File(localFullFileName).isDirectory()) {
                    String remoteSubDir = remoteDstDir + "/" + f;
                    putDir(connection, filter, localFullFileName, remoteSubDir);
                } else {
                    putFile(connection, filter, localFullFileName, remoteDstDir);
                }
            }
        } else {
            putFile(connection, filter, localFile, remoteDstDir);
        }
    }

    private void putFile(SshjConnection connection, FileFilter filter,
                         String localFile, String remoteTargetDir) throws Exception {
        filter.accept(localFile, (accept) -> {
            if (accept) {
                connection.upload(localFile, remoteTargetDir);
            }
        });
    }

    private void close(SshjConnection sshjConnection) {
        if (sshjConnection != null) {
            sshjConnection.close();
        }
    }
}