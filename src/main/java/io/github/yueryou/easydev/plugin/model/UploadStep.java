package io.github.yueryou.easydev.plugin.model;

/**
 * 上传文件步骤
 */
public class UploadStep extends PipelineStep {

    private String uploadProfileId;
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

    public boolean isCreateRemoteDir() {
        return createRemoteDir;
    }

    public void setCreateRemoteDir(boolean createRemoteDir) {
        this.createRemoteDir = createRemoteDir;
    }
}
