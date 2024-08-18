package tech.lin2j.idea.plugin.model.event;

import com.intellij.openapi.project.Project;
import tech.lin2j.idea.plugin.event.ApplicationEvent;
import tech.lin2j.idea.plugin.model.Command;
import tech.lin2j.idea.plugin.ssh.SshServer;

/**
 * @author linjinjia
 * @date 2022/4/29 10:02
 */
public class CommandExecuteEvent extends ApplicationEvent {

    /**
     * the listener should clear the history command result
     * when receives this signal
     */
    public static final int SIGNAL_CLEAR = 1;

    /**
     * the listener should append the coming command result
     * when receives this signal
     */
    public static final int SIGNAL_APPEND = 2;

    private SshServer server;

    private Command command;

    private Boolean success;

    private String execResult;

    private Project project;

    /**
     * the default value is {@link CommandExecuteEvent#SIGNAL_CLEAR}
     *
     * @see CommandExecuteEvent#SIGNAL_CLEAR
     * @see CommandExecuteEvent#SIGNAL_APPEND
     */
    private Integer signal;

    /**
     * it only works when the signal is {@link CommandExecuteEvent#SIGNAL_APPEND},
     * it is used to indicate which message is currently.
     * start from 0
     */
    private Integer index;

    public CommandExecuteEvent(Object source) {
        super(source);
    }

    public CommandExecuteEvent(Project project, Command cmd, SshServer server, String execResult) {
        super(new Object());
        this.command = cmd;
        this.server = server;
        this.execResult = execResult;
        this.signal = SIGNAL_CLEAR;
        this.project = project;
    }

    public CommandExecuteEvent(Project project, SshServer server, String execResult, int index) {
        super(new Object());
        this.server = server;
        this.execResult = execResult;
        this.index = index;
        this.signal = SIGNAL_APPEND;
        this.project = project;
    }

    public String getExecResult() {
        return execResult;
    }

    public void setExecResult(String execResult) {
        this.execResult = execResult;
    }

    public SshServer getServer() {
        return server;
    }

    public void setServer(SshServer server) {
        this.server = server;
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

    public Integer getSignal() {
        return signal;
    }

    public void setSignal(Integer signal) {
        this.signal = signal;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}