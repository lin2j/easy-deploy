package tech.lin2j.idea.plugin.model;


import java.util.Objects;

/**
 * @author linjinjia
 * @date 2022/5/24 15:57
 */
public class UploadProfile implements Cloneable, UniqueModel {

    private Integer id;

    private String uid;

    private String name;

    private Integer sshId;

    private String file;

    /**
     * the suffix name that needs to be excluded
     * during the uploading process
     */
    private String exclude;

    private String location;

    private Integer commandId;

    private Boolean isSelected = false;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSshId() {
        return sshId;
    }

    public void setSshId(Integer sshId) {
        this.sshId = sshId;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getCommandId() {
        return commandId;
    }

    public void setCommandId(Integer commandId) {
        this.commandId = commandId;
    }

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public String getExclude() {
        return exclude;
    }

    public void setExclude(String exclude) {
        this.exclude = exclude;
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public UploadProfile clone() throws CloneNotSupportedException {
        return (UploadProfile) super.clone();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadProfile that = (UploadProfile) o;
        return Objects.equals(sshId, that.sshId)
                && Objects.equals(name, that.name)
                && Objects.equals(file, that.file)
                && Objects.equals(location, that.location)
                && Objects.equals(commandId, that.commandId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sshId, name, file, location, commandId, isSelected);
    }
}