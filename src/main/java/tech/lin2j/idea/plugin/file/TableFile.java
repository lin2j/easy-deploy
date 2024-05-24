package tech.lin2j.idea.plugin.file;

import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

/**
 * @author linjinjia
 * @date 2024/4/6 10:08
 */
public interface TableFile extends Comparable<TableFile> {

    Icon getIcon();

    String getName();

    String getSize();

    String getType();

    String getCreated();

    boolean readOnly();

    String getModified();

    boolean isDirectory();

    default boolean isHidden() {
        return getName().startsWith(".") || getName().startsWith("$");
    }

    String getParent();

    String getFilePath();

    @Override
    default int compareTo(@NotNull TableFile f2) {
        if (this.isDirectory() && !f2.isDirectory()) {
            return -1;
        }
        if (!this.isDirectory() && f2.isDirectory()) {
            return 1;
        }
        return this.getName().compareTo(f2.getName());
    }

    default String getAccess() {
        return "";
    }

    default String getOwner() {
        return "";
    }

    default String getGroup() {
        return "";
    }
}