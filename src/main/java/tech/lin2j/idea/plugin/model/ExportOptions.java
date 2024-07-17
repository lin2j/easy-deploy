package tech.lin2j.idea.plugin.model;

/**
 * @author linjinjia
 * @date 2024/7/17 20:57
 */
public class ExportOptions {

    private boolean command;

    private boolean uploadProfile;

    private boolean serverTags;

    /**
     * always export server info
     */
    public boolean isServer() {
        return true;
    }

    public boolean isCommand() {
        return command;
    }

    public void setCommand(boolean command) {
        this.command = command;
    }

    public boolean isUploadProfile() {
        return uploadProfile;
    }

    public void setUploadProfile(boolean uploadProfile) {
        this.uploadProfile = uploadProfile;
    }

    public boolean isServerTags() {
        return serverTags;
    }

    public void setServerTags(boolean serverTags) {
        this.serverTags = serverTags;
    }
}