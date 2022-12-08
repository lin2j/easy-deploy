package tech.lin2j.idea.plugin.ssh;

/**
 * @author linjinjia
 * @date 2022/4/25 21:13
 */
public class SshStatus {

    private boolean success;

    private String message;

    private Object data;

    public SshStatus(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public SshStatus(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }


    @Override
    public String toString() {
        return "SshStatus{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}