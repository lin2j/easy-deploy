package tech.lin2j.idea.plugin.file;

import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.util.PlatformIcons;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.RemoteResourceInfo;

import javax.swing.Icon;
import java.util.Objects;

/**
 * @author linjinjia
 * @date 2024/4/1 22:52
 */
public class FTPFile {

    private final RemoteResourceInfo remoteResourceInfo;
    private final FileMode.Type type;
    private final Icon icon;

    public FTPFile(RemoteResourceInfo remoteResourceInfo) {
        this.remoteResourceInfo = remoteResourceInfo;
        this.type = this.remoteResourceInfo.getAttributes().getType();
        this.icon = getIcon();
    }

    public String getName() {
        return remoteResourceInfo.getName();
    }

    public boolean isDirectory() {
        return Objects.equals(FileMode.Type.DIRECTORY, type);
    }

    public Icon getIcon() {
        if (icon != null) {
            return icon;
        }
        if (isDirectory()) {
            return PlatformIcons.FOLDER_ICON;
        }
        return FileTypeRegistry.getInstance().getFileTypeByFileName(getName()).getIcon();
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