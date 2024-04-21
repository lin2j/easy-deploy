package tech.lin2j.idea.plugin.file.fileTypes;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * @author linjinjia
 * @date 2024/4/21 11:45
 */
public class SpecifiedArchiveFileType implements FileType {

    private final FileType origin;
    private final String ext;

    public SpecifiedArchiveFileType(FileType origin, String ext) {
        this.origin = origin;
        this.ext = StringUtil.toUpperCase(ext);
    }

    @NotNull
    @Override
    public String getName() {
        return ext + " " + origin.getName();
    }

    @NotNull
    @Override
    public String getDescription() {
        return getName();
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return ext;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return origin.getIcon();
    }

    @Override
    public boolean isBinary() {
        return origin.isBinary();
    }

    @Override
    public boolean isReadOnly() {
        return origin.isReadOnly();
    }

    @Nullable
    @Override
    public String getCharset(@NotNull VirtualFile file, @NotNull byte[] content) {
        return null;
    }
}