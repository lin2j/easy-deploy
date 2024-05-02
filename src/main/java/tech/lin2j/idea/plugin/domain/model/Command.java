package tech.lin2j.idea.plugin.domain.model;

import java.util.Objects;

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
        String newDir = null;
        String newContent = null;
        if (content != null) {
            newContent = content.substring(0, Math.min(content.length(), 30));
            if (newContent.length() < content.length()) {
                newContent += "...";
            }
        }
        if (dir != null) {
            newDir = dir.substring(0, Math.min(dir.length(), 20));
            if (newDir.length() < dir.length()) {
                newDir += "...";
            }
        }

        return String.format("Directory: %s, Command: %s", newDir, newContent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Command command = (Command) o;
        return Objects.equals(title, command.title)
                && Objects.equals(dir, command.dir)
                && Objects.equals(content, command.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, dir, content);
    }
}