package tech.lin2j.idea.plugin.domain.model.event;

import tech.lin2j.idea.plugin.event.ApplicationEvent;

/**
 * @author linjinjia
 * @date 2022/7/25 17:15
 */
public class UploadProfileExecuteEvent extends ApplicationEvent {

    private Boolean success;

    private String uploadResult;

    private Boolean needExecCommand;

    public UploadProfileExecuteEvent() {
        super(new Object());
    }

    public String getUploadResult() {
        return uploadResult;
    }

    public void setUploadResult(String uploadResult) {
        this.uploadResult = uploadResult;
    }

    public Boolean isSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Boolean getNeedExecCommand() {
        return needExecCommand;
    }

    public void setNeedExecCommand(Boolean needExecCommand) {
        this.needExecCommand = needExecCommand;
    }
}