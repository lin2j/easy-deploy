package tech.lin2j.idea.plugin.file.fileTypes;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import icons.MyIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * @author linjinjia
 * @date 2024/4/21 15:19
 */
public class CFileType implements FileType {

    public static final CFileType INSTANCE = new CFileType();

    @NotNull
    @Override
    public String getName() {
        return "C";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "C";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "c";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return MyIcons.FileType.C;
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