package tech.lin2j.idea.plugin.ui.ftp;

import com.intellij.openapi.project.Project;
import net.schmizz.sshj.sftp.SFTPClient;
import tech.lin2j.idea.plugin.file.RemoteTableFile;
import tech.lin2j.idea.plugin.file.TableFile;
import tech.lin2j.idea.plugin.ssh.SshConnectionManager;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ui.table.FileNameCellRenderer;
import tech.lin2j.idea.plugin.ui.table.FileTableModel;

import javax.swing.table.TableColumn;
import java.io.IOException;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author linjinjia
 * @date 2024/4/4 10:19
 */
public class RemoteFileTableContainer extends AbstractFileTableContainer implements FileTableContainer {

    private static final int NAME_COLUMN = 0;

    private final SshServer server;
    private SFTPClient sftpClient;

    public RemoteFileTableContainer(Project project, SshServer server) {
        super(true, project, false);
        this.server = server;
        try {
            this.sftpClient = SshConnectionManager.makeSshClient(server).newSFTPClient();
        } catch (IOException e) {

        }

        init();
    }

    @Override
    public void refreshFileList() {
        if (sftpClient == null) {
            return;
        }
        try {
            fileList = sftpClient.ls(getPath()).stream()
                    .map(RemoteTableFile::new)
                    .filter(this::showFile)
                    .sorted()
                    .collect(Collectors.toList());

            FileTableModel tableModel = new FileTableModel(fileList);
            table.setModel(tableModel);

            TableColumn nameColumn = table.getColumnModel().getColumn(NAME_COLUMN);
            nameColumn.setCellRenderer(new FileNameCellRenderer());
        } catch (Exception e) {

        }
    }

    @Override
    public SFTPClient getFTPClient() {
        return sftpClient;
    }

    @Override
    public String getHomePath() {
        if (Objects.equals("root", server.getUsername())) {
            return "/root";
        }
        return "/home/" + server.getUsername();
    }

    @Override
    public String getParentPath() {
        // More than one '/'
        if (getPath().lastIndexOf('/') > 0){
            return super.getParentPath();
        } else {
            return "/";
        }
    }

    @Override
    public void deleteFileAndDir(TableFile tf) {
        if (sftpClient == null) {
            return;
        }
        try {
            String path = tf.getFilePath();
            if (tf.isDirectory()) {
                sftpClient.rmdir(path);
            } else {
                sftpClient.rm(path);
            }
        } catch (Exception ignored) {

        }

    }

    @Override
    public boolean createNewFolder(String path) throws IOException {
        sftpClient.mkdirs(path);
        return true;
    }
}