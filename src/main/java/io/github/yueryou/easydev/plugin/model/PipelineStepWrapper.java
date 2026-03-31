package io.github.yueryou.easydev.plugin.model;

import com.intellij.util.xmlb.annotations.Tag;

/**
 * PipelineStep 的包装类，用于 XmlSerializer 序列化多态类型
 */
@Tag("step")
public class PipelineStepWrapper {

    private StepType type;
    private String uid;
    private String name;
    private boolean enabled;

    // LocalCommandStep 字段
    private String command;
    private String workingDir;
    private int timeout;
    private String commandId;

    // UploadStep 字段
    private String uploadProfileId;
    private String serverId;
    private boolean createRemoteDir;

    // RemoteCommandStep 字段
    private String remoteCommand;
    private String remoteWorkingDir;
    private String remoteCommandId;
    private String remoteServerId;

    public PipelineStepWrapper() {
    }

    public PipelineStepWrapper(PipelineStep step) {
        this.type = step.getType();
        this.uid = step.getUid();
        this.name = step.getName();
        this.enabled = step.isEnabled();

        if (step instanceof LocalCommandStep) {
            LocalCommandStep localStep = (LocalCommandStep) step;
            this.command = localStep.getCommand();
            this.workingDir = localStep.getWorkingDir();
            this.timeout = localStep.getTimeout();
            this.commandId = localStep.getCommandId();
        } else if (step instanceof UploadStep) {
            UploadStep uploadStep = (UploadStep) step;
            this.uploadProfileId = uploadStep.getUploadProfileId();
            this.serverId = uploadStep.getServerId();
            this.createRemoteDir = uploadStep.isCreateRemoteDir();
        } else if (step instanceof RemoteCommandStep) {
            RemoteCommandStep remoteStep = (RemoteCommandStep) step;
            this.remoteCommand = remoteStep.getCommand();
            this.remoteWorkingDir = remoteStep.getWorkingDir();
            this.remoteCommandId = remoteStep.getCommandId();
            this.remoteServerId = remoteStep.getServerId();
        }
    }

    public PipelineStep toStep() {
        if (type == null) return null;

        PipelineStep step;
        switch (type) {
            case LOCAL_COMMAND:
                LocalCommandStep localStep = new LocalCommandStep();
                localStep.setCommand(command);
                localStep.setWorkingDir(workingDir);
                localStep.setTimeout(timeout);
                localStep.setCommandId(commandId);
                step = localStep;
                break;
            case UPLOAD:
                UploadStep uploadStep = new UploadStep();
                uploadStep.setUploadProfileId(uploadProfileId);
                uploadStep.setServerId(serverId);
                uploadStep.setCreateRemoteDir(createRemoteDir);
                step = uploadStep;
                break;
            case REMOTE_COMMAND:
                RemoteCommandStep remoteStep = new RemoteCommandStep();
                remoteStep.setCommand(remoteCommand);
                remoteStep.setWorkingDir(remoteWorkingDir);
                remoteStep.setCommandId(remoteCommandId);
                remoteStep.setServerId(remoteServerId);
                step = remoteStep;
                break;
            default:
                return null;
        }

        step.setUid(uid);
        step.setName(name);
        step.setEnabled(enabled);
        return step;
    }

    public StepType getType() {
        return type;
    }

    public void setType(StepType type) {
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public String getUploadProfileId() {
        return uploadProfileId;
    }

    public void setUploadProfileId(String uploadProfileId) {
        this.uploadProfileId = uploadProfileId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public boolean isCreateRemoteDir() {
        return createRemoteDir;
    }

    public void setCreateRemoteDir(boolean createRemoteDir) {
        this.createRemoteDir = createRemoteDir;
    }

    public String getRemoteCommand() {
        return remoteCommand;
    }

    public void setRemoteCommand(String remoteCommand) {
        this.remoteCommand = remoteCommand;
    }

    public String getRemoteWorkingDir() {
        return remoteWorkingDir;
    }

    public void setRemoteWorkingDir(String remoteWorkingDir) {
        this.remoteWorkingDir = remoteWorkingDir;
    }

    public String getRemoteCommandId() {
        return remoteCommandId;
    }

    public void setRemoteCommandId(String remoteCommandId) {
        this.remoteCommandId = remoteCommandId;
    }

    public String getRemoteServerId() {
        return remoteServerId;
    }

    public void setRemoteServerId(String remoteServerId) {
        this.remoteServerId = remoteServerId;
    }
}
