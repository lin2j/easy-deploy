package io.github.yueryou.easydev.plugin.model;

/**
 * 远程命令步骤
 */
public class RemoteCommandStep extends PipelineStep {

    private String command;
    private String workingDir;
    private String commandId;

    public RemoteCommandStep() {
        this.type = StepType.REMOTE_COMMAND;
    }

    public RemoteCommandStep(String name) {
        super(name, StepType.REMOTE_COMMAND);
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }
}
