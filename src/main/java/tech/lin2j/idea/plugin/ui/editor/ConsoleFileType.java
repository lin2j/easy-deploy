package tech.lin2j.idea.plugin.ui.editor;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * @author linjinjia
 * @date 2024/3/31 02:24
 */
public class ConsoleFileType implements FileType {
    @Override
    public @NonNls @NotNull String getName() {
        return "FTP Console";
    }

    @Override
    public @NotNull String getDescription() {
        return "A-Ftp console";
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return "";
    }

    @Override
    public @Nullable Icon getIcon() {
        return AllIcons.Nodes.DataColumn;
    }

    @Override
    public boolean isBinary() {
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public @NonNls @Nullable String getCharset(@NotNull VirtualFile file, byte @NotNull [] content) {
        return null;
    }
}