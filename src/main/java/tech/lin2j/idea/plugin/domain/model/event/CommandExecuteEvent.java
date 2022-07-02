package tech.lin2j.idea.plugin.domain.model.event;

import tech.lin2j.idea.plugin.domain.model.Command;
import tech.lin2j.idea.plugin.event.ApplicationEvent;

/**
 * @author linjinjia
 * @date 2022/4/29 10:02
 */
public class CommandExecuteEvent extends ApplicationEvent {

    private Command command;

    private Boolean success;

    private String execResult;

    public CommandExecuteEvent(Object source) {
        super(source);
    }

    public CommandExecuteEvent(Command cmd, String execResult) {
        super(new Object());
        this.command = cmd;
        this.execResult = execResult;
    }

    public String getExecResult() {
        return execResult;
    }

    public void setExecResult(String execResult) {
        this.execResult = execResult;
    }


    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public Boolean isSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}