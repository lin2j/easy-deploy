package tech.lin2j.idea.plugin.model;

import java.util.Objects;

/**
 * @author linjinjia
 * @date 2024/7/17 20:57
 */
public class ExportOptions implements Cloneable {

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

    @Override
    public ExportOptions clone() {
        try {
            return (ExportOptions) super.clone();
        } catch (CloneNotSupportedException e) {
            ExportOptions newOne = new ExportOptions();
            newOne.setServerTags(serverTags);
            newOne.setCommand(command);
            newOne.setUploadProfile(uploadProfile);
            return newOne;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportOptions options = (ExportOptions) o;
        return command == options.command
                && uploadProfile == options.uploadProfile
                && serverTags == options.serverTags;
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, uploadProfile, serverTags);
    }
}