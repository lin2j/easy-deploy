package io.github.yueryou.easydev.plugin.model;

/**
 * 本地命令步骤
 */
public class LocalCommandStep extends PipelineStep {

    private String command;
    private String workingDir;
    private int timeout;
    private String commandId;

    public LocalCommandStep() {
        this.type = StepType.LOCAL_COMMAND;
    }

    public LocalCommandStep(String name) {
        super(name, StepType.LOCAL_COMMAND);
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

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }
}
