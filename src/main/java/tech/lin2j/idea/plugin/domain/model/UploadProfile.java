package tech.lin2j.idea.plugin.domain.model;


/**
 * @author linjinjia
 * @date 2022/5/24 15:57
 */
public class UploadProfile {

    private Long id;

    private String name;

    private Long sshId;

    private String file;

    /**
     * the suffix name that needs to be excluded
     * during the uploading process
     */
    private String exclude;

    private String location;

    private Integer commandId;

    private Boolean isSelected = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSshId() {
        return sshId;
    }

    public void setSshId(Long sshId) {
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
    public String toString() {
        return name;
    }
}