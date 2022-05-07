package tech.lin2j.idea.plugin.domain.model;

/**
 * @author linjinjia
 * @date 2022/4/25 21:13
 */
public class SshStatus {

    private boolean success;

    private String message;

    public SshStatus(boolean success, String message) {
        this.success = success;
        this.message = message;
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

    @Override
    public String toString() {
        return "SshStatus{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}