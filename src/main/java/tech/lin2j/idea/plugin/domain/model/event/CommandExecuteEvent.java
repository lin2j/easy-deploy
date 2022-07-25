package tech.lin2j.idea.plugin.domain.model.event;

import com.intellij.openapi.project.Project;
import tech.lin2j.idea.plugin.domain.model.Command;
import tech.lin2j.idea.plugin.event.ApplicationEvent;
import tech.lin2j.idea.plugin.ssh.SshServer;

/**
 * @author linjinjia
 * @date 2022/4/29 10:02
 */
public class CommandExecuteEvent extends ApplicationEvent {

    private Project project;

    private SshServer server;

    private Command command;

    private Boolean success;

    private String execResult;

    public CommandExecuteEvent(Object source) {
        super(source);
    }

    public CommandExecuteEvent(Command cmd, SshServer server,
                               Project project, String execResult) {
        super(new Object());
        this.project = project;
        this.command = cmd;
        this.server = server;
        this.execResult = execResult;
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

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }
}