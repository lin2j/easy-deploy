package tech.lin2j.idea.plugin.ui.ftp.container;

import com.intellij.openapi.project.Project;
import com.intellij.ui.table.JBTable;
import net.schmizz.sshj.sftp.SFTPClient;
import tech.lin2j.idea.plugin.file.TableFile;

import java.io.IOException;
import java.util.List;

/**
 * @author linjinjia
 * @date 2024/4/6 21:11
 */
public interface FileTableContainer {

    JBTable getTable();

    List<TableFile> getFileList();

    List<TableFile> getSelectedFiles();

    void setPath(String path);

    String getPath();

    String getHomePath();

    String getParentPath();

    void deleteFileAndDir(TableFile tf);

    boolean createNewFolder(String path) throws IOException;

    boolean showHiddenFileAndDir();

    void reversedHiddenFlag();

    void refreshFileList();

    Project getProject();

    default SFTPClient getFTPClient() {
        throw new UnsupportedOperationException();
    }
}