package tech.lin2j.idea.plugin.domain.model;

import java.util.Objects;

/**
 * @author linjinjia
 * @date 2022/5/7 09:28
 */
public class SshUpload {

    private Integer sshId;

    private String localFile;

    private String remoteFile;

    private Boolean isSelected;

    public SshUpload() {
        isSelected = false;
    }

    public SshUpload(Integer sshId, String localFile, String remoteFile) {
        this.sshId = sshId;
        this.localFile = localFile;
        this.remoteFile = remoteFile;
        isSelected = false;
    }

    public Integer getSshId() {
        return sshId;
    }

    public void setSshId(Integer sshId) {
        this.sshId = sshId;
    }

    public String getLocalFile() {
        return localFile;
    }

    public void setLocalFile(String localFile) {
        this.localFile = localFile;
    }

    public String getRemoteFile() {
        return remoteFile;
    }

    public void setRemoteFile(String remoteFile) {
        this.remoteFile = remoteFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SshUpload sshUpload = (SshUpload) o;

        if (!Objects.equals(sshId, sshUpload.sshId)) {
            return false;
        }
        if (!Objects.equals(localFile, sshUpload.localFile)) {
            return false;
        }
        return Objects.equals(remoteFile, sshUpload.remoteFile);
    }

    @Override
    public int hashCode() {
        int result = sshId != null ? sshId.hashCode() : 0;
        result = 31 * result + (localFile != null ? localFile.hashCode() : 0);
        result = 31 * result + (remoteFile != null ? remoteFile.hashCode() : 0);
        return result;
    }

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }
}