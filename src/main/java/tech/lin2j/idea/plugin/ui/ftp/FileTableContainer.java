package tech.lin2j.idea.plugin.ui.ftp;

import com.intellij.ui.table.JBTable;
import tech.lin2j.idea.plugin.file.TableFile;

import java.util.List;

/**
 * @author linjinjia
 * @date 2024/4/6 21:11
 */
public interface FileTableContainer {

    JBTable getTable();

    List<TableFile> getFileList();

    void setPath(String path);

    String getPath();
}