package tech.lin2j.idea.plugin.model;

import java.util.Objects;

/**
 * @author linjinjia
 * @date 2022/4/27 14:01
 */
public class Command implements Cloneable, UniqueModel {

    private Integer id;

    private String uid;

    private Integer sshId;

    private String title;

    private String dir;

    private String content;

    private Boolean sharable;

    public Command() {
    }

    public Command(Integer sshId) {
        this.sshId = sshId;
    }

    public Command(Integer id, Integer sshId, String title,
                   String dir, String content, Boolean sharable) {
        super();
        this.id = id;
        this.sshId = sshId;
        this.title = title;
        this.dir = dir;
        this.content = content;
        this.sharable = sharable;
    }

    public String generateCmdLine() {
        return "cd " + dir + "; " + content;
    }

    public String generateCmdLine(String overrideDir) {
        String workingDir = (overrideDir != null && !overrideDir.trim().isEmpty()) ? overrideDir : dir;
        return "cd " + workingDir + "; " + content;
    }

    public String logString() {
        StringBuilder buf = new StringBuilder();
        buf.append("cd ").append(dir).append(";\n");
        if (content != null) {
            String[] line = content.split("\n");
            for (String s : line) {
                buf.append(s).append("\n");
            }
        }
        return buf.toString();
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

    public Boolean getSharable() {
        if (sharable == null) {
            sharable = false;
        }
        return sharable;
    }

    public void setSharable(Boolean sharable) {
        this.sharable = sharable;
    }

    @Override
    public Command clone() throws CloneNotSupportedException {
        return (Command) super.clone();
    }

    @Override
    public String toString() {
        return toDisplayString(null);
    }

    public String toDisplayString(String overrideDir) {
        String displayDir = (overrideDir != null && !overrideDir.trim().isEmpty()) ? overrideDir : dir;
        String newDir = null;
        String newContent = null;
        if (content != null) {
            newContent = content.substring(0, Math.min(content.length(), 30));
            if (newContent.length() < content.length()) {
                newContent += "...";
            }
        }
        if (displayDir != null) {
            newDir = displayDir.substring(0, Math.min(displayDir.length(), 40));
            if (newDir.length() < displayDir.length()) {
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
        return Objects.equals(sshId, command.sshId)
                && Objects.equals(title, command.title)
                && Objects.equals(dir, command.dir)
                && Objects.equals(content, command.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sshId, title, dir, content);
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public void setUid(String uid) {
        this.uid = uid;
    }
}