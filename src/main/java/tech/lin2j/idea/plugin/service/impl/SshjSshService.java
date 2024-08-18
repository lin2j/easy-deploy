package tech.lin2j.idea.plugin.service.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import net.schmizz.sshj.xfer.TransferListener;
import tech.lin2j.idea.plugin.file.EventFileFilterAdapter;
import tech.lin2j.idea.plugin.file.ExtensionFilter;
import tech.lin2j.idea.plugin.file.FileFilter;
import tech.lin2j.idea.plugin.service.ISshService;
import tech.lin2j.idea.plugin.ssh.SshConnectionManager;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.SshStatus;
import tech.lin2j.idea.plugin.ssh.sshj.SshjConnection;

import java.io.File;

/**
 * @author linjinjia
 * @date 2024/1/5 21:14
 */
public class SshjSshService implements ISshService {

    private static final Logger log = Logger.getInstance(SshjSshService.class);

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
            log.error(e.getMessage(), e);
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
            log.error(e.getMessage(), e);
            return new SshStatus(false, e.getMessage());
        } finally {
            close(sshjConnection);
        }
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
            log.error(e.getMessage(), e);
            msg = e.getMessage();
        } finally {
            close(sshjConnection);
        }
        return new SshStatus(status, msg);
    }

    @Override
    public SshStatus upload(FileFilter filter, SshServer sshServer,
                            String localFile, String remoteDir,
                            TransferListener listener) {
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
            log.error(e.getMessage(), e);
            status.setSuccess(false);
            status.setMessage(e.getMessage());
        } finally {
            close(sshjConnection);
        }
        return status;
    }

    @Override
    public SshStatus upload(Project project, SshServer sshServer, String localFile, String remoteDir, String exclude) {
        String cmd = "upload [" + localFile + "] to [" + remoteDir + "]";
        FileFilter filter = new EventFileFilterAdapter(project, new ExtensionFilter(exclude), sshServer, cmd);
        return upload(filter, sshServer, localFile, remoteDir, null);
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