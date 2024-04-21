package tech.lin2j.idea.plugin.ui.ftp.container;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.util.ExecUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.SystemProperties;
import tech.lin2j.idea.plugin.file.LocalTableFile;
import tech.lin2j.idea.plugin.file.TableFile;
import tech.lin2j.idea.plugin.ui.table.FileNameCellRenderer;
import tech.lin2j.idea.plugin.ui.table.LocalFileTableModel;

import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author linjinjia
 * @date 2024/4/4 10:19
 */
public class LocalFileTableContainer extends AbstractFileTableContainer implements FileTableContainer{

    private static final int NAME_COLUMN = 0;

    public LocalFileTableContainer(Project project) {
        super(true, project, true);
        init();
    }

    @Override
    public String getHomePath() {
        return SystemProperties.getUserHome();
    }

    @Override
    public void deleteFileAndDir(TableFile tf) {
        FileUtil.delete(new File(tf.getFilePath()));
    }

    @Override
    public boolean createNewFolder(String path) {
         return FileUtil.createDirectory(new File(path));
    }

    public VirtualFile getDesktopDirectory() {
        File desktop = new File(SystemProperties.getUserHome(), "Desktop");

        if (!desktop.isDirectory() && SystemInfo.hasXdgOpen()) {
            String path = ExecUtil.execAndReadLine(new GeneralCommandLine("xdg-user-dir", "DESKTOP"));
            if (path != null) {
                desktop = new File(path);
            }
        }

        return desktop.isDirectory() ? LocalFileSystem.getInstance().refreshAndFindFileByIoFile(desktop) : null;
    }

    @Override
    public void refreshFileList() {
        File file = new File(filePath.getText());
        if (!file.exists() || !file.isDirectory()) {
            Messages.showErrorDialog("Specified path not found.", "Path");
            return;
        }
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        fileList = Arrays.stream(files)
                .map(LocalTableFile::new)
                .filter(this::showFile)
                .sorted()
                .collect(Collectors.toList());

        LocalFileTableModel tableModel = new LocalFileTableModel(fileList);
        table.setModel(tableModel);

        TableColumn nameColumn = table.getColumnModel().getColumn(NAME_COLUMN);
        nameColumn.setCellRenderer(new FileNameCellRenderer());
    }

    @Override
    protected TableModel getTableModel() {
        return new LocalFileTableModel(Collections.emptyList());
    }
}