package tech.lin2j.idea.plugin.file;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.PlatformIcons;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import org.apache.commons.lang.time.DateFormatUtils;

import javax.swing.Icon;
import java.util.Objects;

/**
 * @author linjinjia
 * @date 2024/4/1 22:52
 */
public class RemoteTableFile implements TableFile {

    private final RemoteResourceInfo remoteResourceInfo;
    private final FileMode.Type type;
    private final Icon icon;
    private final FileType fileType;

    public RemoteTableFile(RemoteResourceInfo remoteResourceInfo) {
        this.remoteResourceInfo = remoteResourceInfo;
        this.type = this.remoteResourceInfo.getAttributes().getType();
        this.fileType = FileTypeRegistry.getInstance().getFileTypeByFileName(getName());
        this.icon = getIcon();
    }

    @Override
    public String getName() {
        return remoteResourceInfo.getName();
    }

    @Override
    public String getSize() {
        return StringUtil.formatFileSize(remoteResourceInfo.getAttributes().getSize());
    }

    @Override
    public String getType() {
        return isDirectory() ? "Folder" : fileType.getName();
    }

    @Override
    public String getModified() {
        long time = remoteResourceInfo.getAttributes().getAtime() * 1000;
        return DateFormatUtils.format(time, "yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public boolean isDirectory() {
        return Objects.equals(FileMode.Type.DIRECTORY, type);
    }

    @Override
    public String getFilePath() {
        return remoteResourceInfo.getPath();
    }

    @Override
    public String getParent() {
        return remoteResourceInfo.getParent();
    }

    @Override
    public Icon getIcon() {
        if (icon != null) {
            return icon;
        }
        if (isDirectory()) {
            return PlatformIcons.FOLDER_ICON;
        }
        return fileType.getIcon();
    }

    @Override
    public String toString() {
        return "FTPFile{" +
                "remoteResourceInfo=" + remoteResourceInfo +
                ", type=" + type +
                ", icon=" + icon +
                '}';
    }
}