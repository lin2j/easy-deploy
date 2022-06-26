package tech.lin2j.idea.plugin.ssh.exception;

/**
 * @author linjinjia
 * @date 2022/6/26 14:09
 */
public class RemoteSdkException extends RuntimeException{

    public RemoteSdkException(String msg, Throwable e) {
        super(msg, e);
    }

    public RemoteSdkException(String msg) {
        super(msg);
    }
}