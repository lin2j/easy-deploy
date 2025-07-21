package tech.lin2j.idea.plugin.ssh;


import java.io.IOException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author linjinjia
 * @date 2022/6/24 20:46
 */
public interface SshConnection {

    /**
     * Return whether the SSH connection has been successfully established.
     * @return true if established, false otherwise
     */
    boolean isConnected();

    /**
     * upload file from local to remote server
     * @param local the absolute path of local file
     * @param dest the absolute path of remote destination
     */
    void upload(String local, String dest) throws IOException;

    /**
     * download file from remote server
     * @param remote the absolute path of remote file
     * @param dest the absolute path that the file will be stored
     */
    void download(String remote, String dest) throws IOException;

    void scpUpload(String local, String dest) throws IOException;

    void scpDownload(String remote, String dest) throws IOException;

    /**
     * execute command </br>
     * note that command like 'tail -f' will block the thread
     * @param cmd command
     * @return the execution result of the command, some commands
     * return results while others indicate errors, requiring the
     * caller to discern
     */
    SshStatus execute(String cmd) throws IOException;

    /**
     * execute command with real-time output to command log
     * @param cmd command to execute
     * @param commandLog command log for real-time output
     * @return the execution result of the command
     */
    SshStatus execute(String cmd, CommandLog commandLog) throws IOException;

    FutureTask<Void> executeAsync(CommandLog commandLog, String cmd,
                                  AtomicBoolean cancel, boolean closeAfterFinished);

    /**
     * Create a directory, automatically creating parent directories
     * if they do not exist.
     *
     * @param dir directory path
     * @throws IOException IOException
     */
    void mkdirs(String dir) throws IOException;

    /**
     * close the ssh connection
     */
    void close();

    /**
     * return true when the connection has closed, otherwise return false;
     * @return turn only if the connection has closed, otherwise return false
     */
    boolean isClosed();
}