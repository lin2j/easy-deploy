package tech.lin2j.idea.plugin.ssh;


/**
 * @author linjinjia
 * @date 2022/6/24 20:46
 */
public interface SshConnection {

    /**
     * upload file
     * @param local the absolute path of local file
     * @param dest the absolute path of remote destination
     * @return status
     */
    SshStatus upload(String local, String dest);

    /**
     * download file from remote server
     * @param remote the absolute path of remote file
     * @param dest the absolute path that the file will be stored
     */
    void download(String remote, String dest);

    /**
     * execute command </br>
     * note that command like 'tail -f' will block the thread
     * @param cmd command
     * @return status
     */
    SshStatus execute(String cmd);

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