package tech.lin2j.idea.plugin.service;

import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.SshStatus;

/**
 * @author linjinjia
 * @date 2023/12/21 22:20
 */
public interface ISshService {

    /**
     * test whether the server information is available
     *
     * @param sshServer server information
     * @return execution result
     */
    SshStatus isValid(SshServer sshServer);

    /**
     * block if the command is not finished,
     * so do not execute command like "tail -f"
     * because it will block the thread
     *
     * @param sshServer server information
     * @param command   command
     * @return execution result
     */
    SshStatus execute(SshServer sshServer, String command);

    /**
     * get file from remote server
     *
     * @param sshServer  server information
     * @param remoteFile remote file absolute path
     * @param localFile  local file absolute path
     * @return download result
     */
    SshStatus scpGet(SshServer sshServer, String remoteFile, String localFile);

    /**
     * upload file to remote server
     *
     * @param sshServer server information
     * @param localFile local file absolute path,
     *                  if it is a directory, then
     *                  upload all files in this directory
     * @param remoteDir remote file absolute path
     * @param exclude   the suffix name that needs to be excluded
     *                  during the uploading process. Only when
     *                  uploading the folder will it be used
     * @return upload result
     */
    SshStatus scpPut(SshServer sshServer, String localFile, String remoteDir, String exclude);


    /**
     * test whether the remote target directory is exist.
     *
     * @param server          ssh server information
     * @param remoteTargetDir remote target directory
     * @return return true if the remote target directory is exist, or return false
     */
    SshStatus isDirExist(SshServer server, String remoteTargetDir);

}