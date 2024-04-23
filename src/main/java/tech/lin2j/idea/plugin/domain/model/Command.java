package tech.lin2j.idea.plugin.domain.model;

/**
 * @author linjinjia
 * @date 2022/4/27 14:01
 */
public class Command {

    private Integer id;

    private Integer sshId;

    private String title;

    private String dir;

    private String content;

    public Command() {
    }

    public Command(Integer id, Integer sshId, String title, String dir, String content) {
        super();
        this.id = id;
        this.sshId = sshId;
        this.title = title;
        this.dir = dir;
        this.content = content;
    }

    public String generateCmdLine() {
        return "cd " + dir + "; " +
                String.join("; ", content.split("\n"));
    }

    public Integer getSshId() {
        return sshId;
    }

    public void setSshId(Integer sshId) {
        this.sshId = sshId;
    }


    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        String newContent = null;
        if (content != null) {
            newContent = content.substring(0, Math.min(content.length(), 30));
        }
        return String.format("Command: %s, Directory: %s", newContent, dir);
    }
}