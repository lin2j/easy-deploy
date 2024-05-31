package tech.lin2j.idea.plugin.file;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PlatformIcons;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.xfer.FilePermission;
import org.apache.commons.lang.time.DateFormatUtils;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.uitl.FileUtil;
import tech.lin2j.idea.plugin.uitl.PosixUtil;

import javax.swing.Icon;
import java.util.Objects;
import java.util.Set;

/**
 * @author linjinjia
 * @date 2024/4/1 22:52
 */
public class RemoteTableFile implements TableFile {

    private final RemoteResourceInfo remoteResourceInfo;
    private final FileMode.Type type;
    private final SshServer server;
    private FileType fileType;
    private final Icon icon;

    public RemoteTableFile(SshServer server, RemoteResourceInfo remoteResourceInfo) {
        this.server = server;
        this.remoteResourceInfo = remoteResourceInfo;
        this.type = this.remoteResourceInfo.getAttributes().getType();
        this.fileType = FileUtil.getFileType(getName());
        this.icon = getIcon();
    }

    @Override
    public String getName() {
        return remoteResourceInfo.getName();
    }

    @Override
    public String getSize() {
        if (isDirectory()) {
            return "";
        }
        return StringUtil.formatFileSize(remoteResourceInfo.getAttributes().getSize());
    }

    @Override
    public String getType() {
        return isDirectory() ? "Folder" : fileType.getName();
    }

    @Override
    public String getCreated() {
        return "";
    }

    @Override
    public boolean readOnly() {
        return false;
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
    public String getAccess() {
        Set<FilePermission> perms = remoteResourceInfo.getAttributes().getPermissions();
        return PosixUtil.toString(perms);
    }

    @Override
    public String getOwner() {
        int uid = remoteResourceInfo.getAttributes().getUID();
        return PosixUtil.getUser(server, uid);
    }

    @Override
    public String getGroup() {
        int uid = remoteResourceInfo.getAttributes().getUID();
        return PosixUtil.getGroup(server, uid);
    }

    public Set<FilePermission> getFilePermissions() {
        return remoteResourceInfo.getAttributes().getPermissions();
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