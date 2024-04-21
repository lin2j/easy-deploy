package tech.lin2j.idea.plugin.file.fileTypes;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * @author linjinjia
 * @date 2024/4/21 15:19
 */
public class ConfigFileType implements FileType {

    public static final ConfigFileType INSTANCE = new ConfigFileType();

    @NotNull
    @Override
    public String getName() {
        return "Config";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Config";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return null;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return AllIcons.FileTypes.Config;
    }

    @Override
    public boolean isBinary() {
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Nullable
    @Override
    public String getCharset(@NotNull VirtualFile file, @NotNull byte[] content) {
        return null;
    }
}