package tech.lin2j.idea.plugin.model;

import tech.lin2j.idea.plugin.enums.Constant;
import tech.lin2j.idea.plugin.ssh.SshServer;

import java.util.Objects;

/**
 * @author linjinjia
 * @date 2024/6/2 18:03
 */
public class DeployProfile {

    public static final int ACTIVE_IDX = 0;
    public static final int SSH_IDX = 1;
    public static final int UPLOAD_PROFILE_IDX = 2;

    private boolean active;
    private Integer sshId;
    private Integer profileId;

    private SshServer server;
    private UploadProfile uploadProfile;
    private Command command;

    public DeployProfile(String profile) {
        int[] resolvedProfile = resolveProfile(profile);
        this.active = resolvedProfile[ACTIVE_IDX] == Constant.INT_TRUE;
        this.sshId = resolvedProfile[SSH_IDX];
        this.profileId = resolvedProfile[UPLOAD_PROFILE_IDX];

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

    /**
     * Resolve the profile string into an array of integers.
     * The format of the profile string is expected to be: "active@sshId@profileId"
     * </p>
     * Where:
     * <ul>
     *     <li>active: 1 for active, 0 for inactive (default is 1 if not specified)</li>
     *     <li>sshId: the ID of the SSH server</li>
     *     <li>profileId: the ID of the upload profile</li>
     * </ul>
     * Example:
     * <ul>
     *     <li>"1@2@3" would resolve to [1, 2, 3]</li>
     *     <li>"2@3" would resolve to [1, 2, 3]</li>
     * </ul>
     *
     * @param profile the profile string to resolve
     * @return an array of integers
     */
    public static int[] resolveProfile(String profile) {
        int[] result = new int[3];
        String[] ss = profile.split("@");
        int i = 0;
        if (ss.length == 3) {
            result[ACTIVE_IDX] = Integer.parseInt(ss[i++]);
        } else {
            result[ACTIVE_IDX] = 1;
        }
        result[SSH_IDX] = Integer.parseInt(ss[i++]);
        result[UPLOAD_PROFILE_IDX] = Integer.parseInt(ss[i]);
        return result;
    }
}