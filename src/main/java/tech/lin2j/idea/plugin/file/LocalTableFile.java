package tech.lin2j.idea.plugin.file;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import org.apache.commons.lang.time.DateFormatUtils;
import tech.lin2j.idea.plugin.uitl.FileUtil;

import javax.swing.Icon;
import java.awt.Panel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

/**
 * @author linjinjia
 * @date 2024/4/6 10:18
 */
public class LocalTableFile implements TableFile {
    private final File vf;
    private final FileType fileType;

    private BasicFileAttributes basicFileAttributes;

    public LocalTableFile(File vf) {
        this.vf = vf;
        this.fileType = FileUtil.getFileType(getName());
        if (SystemInfo.isWindows) {
            try {
                basicFileAttributes = Files.readAttributes(vf.toPath(), BasicFileAttributes.class);
            } catch (Exception ignored) {

            }
        }
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
        if (isDirectory()) {
            return "";
        }
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
        return PathUtil.getParentPath(vf.getPath());
    }

    @Override
    public boolean isHidden() {
        return vf.isHidden();
    }

    @Override
    public String getCreated() {
        if (basicFileAttributes != null) {
            long ct = basicFileAttributes.creationTime().toMillis();
            return DateFormatUtils.format(new Date(ct), "yyyy-MM-dd HH:mm:ss");
        }
        return "";
    }

    @Override
    public boolean readOnly() {
        return !vf.canWrite();
    }
}