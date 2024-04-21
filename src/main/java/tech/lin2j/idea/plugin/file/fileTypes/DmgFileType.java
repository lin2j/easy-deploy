package tech.lin2j.idea.plugin.file.fileTypes;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import icons.MyIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * @author linjinjia
 * @date 2024/4/21 13:23
 */
public class DmgFileType implements FileType {

    public static final DmgFileType INSTANCE = new DmgFileType();

    @NotNull
    @Override
    public String getName() {
        return "Disk Image";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Disk Image";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "dmg";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return MyIcons.FileType.DMG;
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