package tech.lin2j.idea.plugin.model;

import tech.lin2j.idea.plugin.ssh.SshServer;

import java.util.Objects;

/**
 * @author linjinjia
 * @date 2024/6/2 18:03
 */
public class DeployProfile {

    private boolean active;
    private Integer sshId;
    private Integer profileId;

    private SshServer server;
    private UploadProfile uploadProfile;
    private Command command;

    public DeployProfile(String profile) {
        String[] ss = profile.split("@");
        int i = 0;
        if (ss.length == 3) {
            this.active = Integer.parseInt(ss[i++]) == 1;
        } else {
            this.active = true;
        }
        this.sshId = Integer.parseInt(ss[i++]);
        this.profileId = Integer.parseInt(ss[i]);

        this.server = ConfigHelper.getSshServerById(sshId);
        this.uploadProfile = ConfigHelper.getOneUploadProfileById(sshId, profileId);

        Command cmd;
        Integer cmdId = uploadProfile.getCommandId();
        if (cmdId == null) {
            cmd = NoneCommand.INSTANCE;
        } else {
            cmd = ConfigHelper.getCommandById(uploadProfile.getCommandId());
            if (cmd == null) {
                cmd = NoneCommand.INSTANCE;
            }
        }
        this.command = cmd;
    }

    @Override
    public String toString() {
        return (active ? 1 : 0) + "@" + sshId + "@" + profileId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getSshId() {
        return sshId;
    }

    public void setSshId(Integer sshId) {
        this.sshId = sshId;
    }

    public Integer getProfileId() {
        return profileId;
    }

    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
    }

    public SshServer getServer() {
        return server;
    }

    public void setServer(SshServer server) {
        this.server = server;
    }

    public UploadProfile getUploadProfile() {
        return uploadProfile;
    }

    public void setUploadProfile(UploadProfile uploadProfile) {
        this.uploadProfile = uploadProfile;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeployProfile that = (DeployProfile) o;
        return Objects.equals(sshId, that.sshId) && Objects.equals(profileId, that.profileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sshId, profileId);
    }
}