package tech.lin2j.idea.plugin.uitl;

import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import tech.lin2j.idea.plugin.file.DirectoryInfo;

import java.io.IOException;
import java.util.List;

/**
 * @author linjinjia
 * @date 2024/4/14 14:54
 */
public class FTPUtil {

    /**
     * Calculates the size of a directory on a remote FTP server recursively.
     *
     * @param ftpClient The FTP client connected to the server.
     * @param remoteDir The remote directory whose size needs to be calculated.
     * @return The total size of the remote directory and its contents, in bytes.
     * @throws IOException If an I/O error occurs while accessing the FTP server.
     */
    public static DirectoryInfo calcDirectorySize(SFTPClient ftpClient, String remoteDir) throws IOException {
        DirectoryInfo di = new DirectoryInfo();
        FileMode.Type type = ftpClient.type(remoteDir);
        if (type != FileMode.Type.DIRECTORY) {
            di.setDirectory(false);
            di.setSize(ftpClient.stat(remoteDir).getSize());
            di.setFiles(1);
            return di;
        }
        di.setDirectory(true);
        List<RemoteResourceInfo> files = ftpClient.ls(remoteDir);
        if (files != null) {
            for (RemoteResourceInfo file : files) {
                DirectoryInfo subDi = calcDirectorySize(ftpClient, remoteDir + "/" + file.getName());
                di.setFiles(di.getFiles() + subDi.getFiles());
                di.setSize(di.getSize() + subDi.getSize());
            }
        }
        return di;
    }
}