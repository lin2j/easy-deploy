package io.github.yueryou.easydev.plugin.model;

import com.intellij.util.xmlb.annotations.Tag;

/**
 * 上传文件步骤
 */
@Tag("upload-step")
public class UploadStep extends PipelineStep {

    private String uploadProfileId;
    private String serverId;
    private boolean createRemoteDir;

    public UploadStep() {
        this.type = StepType.UPLOAD;
    }

    public UploadStep(String name) {
        super(name, StepType.UPLOAD);
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
}
