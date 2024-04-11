package tech.lin2j.idea.plugin.file;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import org.apache.commons.lang.time.DateFormatUtils;

import javax.swing.Icon;
import java.io.File;

/**
 * @author linjinjia
 * @date 2024/4/6 10:18
 */
public class LocalTableFile implements TableFile {
    private final File vf;
    private final FileType fileType;

    public LocalTableFile(File vf) {
        this.vf = vf;
        this.fileType = FileTypeRegistry.getInstance().getFileTypeByFileName(vf.getName());
    }

    @Override
    public Icon getIcon() {
        return vf.isDirectory() ? AllIcons.Nodes.Folder : fileType.getIcon();
    }

    @Override
    public String getName() {
        return vf.getName();
    }

    @Override
    public String getSize() {
        return StringUtil.formatFileSize(vf.length());
    }

    @Override
    public String getType() {
        return vf.isDirectory() ? "Folder" : fileType.getName();
    }

    @Override
    public String getModified() {
        return DateFormatUtils.format(vf.lastModified(), "yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public boolean isDirectory() {
        return vf.isDirectory();
    }

    @Override
    public String getFilePath() {
        return vf.getPath();
    }

    @Override
    public String getParent() {
//        VirtualFile parent = vf.getParent();
        return PathUtil.getParentPath(vf.getPath());
    }
}