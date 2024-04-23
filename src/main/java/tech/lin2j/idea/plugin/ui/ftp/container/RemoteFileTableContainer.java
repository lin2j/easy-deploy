package tech.lin2j.idea.plugin.ui.ftp.container;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.SFTPClient;
import tech.lin2j.idea.plugin.file.RemoteTableFile;
import tech.lin2j.idea.plugin.file.TableFile;
import tech.lin2j.idea.plugin.ssh.SshConnectionManager;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ui.table.FileNameCellRenderer;
import tech.lin2j.idea.plugin.ui.table.RemoteFileTableModel;

import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author linjinjia
 * @date 2024/4/4 10:19
 */
public class RemoteFileTableContainer extends AbstractFileTableContainer implements FileTableContainer {

    private static final int NAME_COLUMN = 0;
    private static final int MODIFIED_COLUMN = 3;

    private final SshServer server;
    private SFTPClient sftpClient;

    public RemoteFileTableContainer(Project project, SshServer server) {
        super(true, project, false);
        this.server = server;
        try {
            this.sftpClient = SshConnectionManager.makeSshClient(server).newSFTPClient();
        } catch (IOException ignored) {
        }

        init();
    }

    @Override
    public void refreshFileList() {
        try {
            if (sftpClient == null) {
                this.sftpClient = SshConnectionManager.makeSshClient(server).newSFTPClient();
            }
            FileAttributes atts = sftpClient.stat(getPath());
            if (atts == null || atts.getType() != FileMode.Type.DIRECTORY) {
                Messages.showErrorDialog("Specified path not found.", "Path");
                return;
            }
            fileList = sftpClient.ls(getPath()).stream()
                    .map(rf -> new RemoteTableFile(server, rf))
                    .filter(this::showFile)
                    .sorted()
                    .collect(Collectors.toList());

            RemoteFileTableModel tableModel = new RemoteFileTableModel(fileList);
            table.setModel(tableModel);

            TableColumn nameColumn = table.getColumnModel().getColumn(NAME_COLUMN);
            nameColumn.setCellRenderer(new FileNameCellRenderer());
            nameColumn.setMinWidth(200);

            TableColumn modifiedColumn = table.getColumnModel().getColumn(MODIFIED_COLUMN);
            modifiedColumn.setMinWidth(150);
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                Messages.showErrorDialog("The specified path cannot be opened: " + e.getMessage(), "Path");
            });
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
        String parent = super.getParentPath();
        if (StringUtil.isEmpty(parent)) {
            parent = "/";
        }
        return parent;
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

    @Override
    protected TableModel getTableModel() {
        return new RemoteFileTableModel(Collections.emptyList());
    }
}