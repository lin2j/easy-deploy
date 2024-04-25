package tech.lin2j.idea.plugin.file.fileTypes;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import icons.MyIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * @author linjinjia
 * @date 2024/4/21 13:31
 */
public class VideoFileType implements FileType {

    private final String ext;

    public VideoFileType(String ext) {
        this.ext = StringUtil.toUpperCase(ext);
    }

    @NotNull
    @Override
    public String getName() {
        return ext;
    }

    @NotNull
    @Override
    public String getDescription() {
        return ext + " Video";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return ext;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return MyIcons.FileType.Video;
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