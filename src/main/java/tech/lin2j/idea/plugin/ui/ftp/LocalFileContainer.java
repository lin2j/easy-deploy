package tech.lin2j.idea.plugin.ui.ftp;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import tech.lin2j.idea.plugin.action.ftp.local.CreateNewFolderAction;
import tech.lin2j.idea.plugin.action.ftp.local.DeleteFileAndDirAction;
import tech.lin2j.idea.plugin.action.ftp.local.GoToDesktopAction;
import tech.lin2j.idea.plugin.action.ftp.local.GoToParentFolderAction;
import tech.lin2j.idea.plugin.action.ftp.local.HomeDirectoryAction;
import tech.lin2j.idea.plugin.action.ftp.local.RefreshFolderAction;
import tech.lin2j.idea.plugin.action.ftp.RowDoubleClickAction;
import tech.lin2j.idea.plugin.action.ftp.local.ShowHiddenFileAndDirAction;
import tech.lin2j.idea.plugin.action.ftp.local.UploadFileAndDirAction;
import tech.lin2j.idea.plugin.file.LocalTableFile;
import tech.lin2j.idea.plugin.file.TableFile;
import tech.lin2j.idea.plugin.ui.table.FileNameCellRenderer;
import tech.lin2j.idea.plugin.ui.table.FileTableModel;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author linjinjia
 * @date 2024/4/4 10:19
 */
public class LocalFileContainer extends SimpleToolWindowPanel implements FileTableContainer{

    private static final int NAME_COLUMN = 0;

    private JBTextField filePath;
    private JBTable table;
    private List<TableFile> fileList;

    public LocalFileContainer(Project project) {
        super(true);
        init();
    }

    public String getPath() {
        return filePath.getText();
    }

    public void setPath(String path) {
        filePath.setText(path);
        refreshFileList();
    }

    public JBTable getTable() {
        return table;
    }

    public List<TableFile> getFileList() {
        return fileList;
    }

    public void refreshFileList() {
        File file = new File(filePath.getText());
        if (!file.exists() || !file.isDirectory()) {
            Messages.showErrorDialog("Specified path not found.", "Path");
            return;
        }
        VirtualFile vFile = LocalFileSystem.getInstance().findFileByIoFile(file);
        if (vFile == null) {
            return;
        }
        fileList = Arrays.stream(vFile.getChildren())
                .sorted(Comparator.comparing(VirtualFile::getName))
                .map(LocalTableFile::new)
                .filter(f -> !f.isHidden())
                .collect(Collectors.toList());

        FileTableModel tableModel = new FileTableModel(fileList);
        table.setModel(tableModel);

        TableColumn nameColumn = table.getColumnModel().getColumn(NAME_COLUMN);
        nameColumn.setCellRenderer(new FileNameCellRenderer());
    }

    private void init() {
        filePath = new JBTextField();
        filePath.setText(System.getProperty("user.home"));

        initToolBar();
        initFileTable();
    }

    private void initToolBar() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new HomeDirectoryAction(this));
        if (SystemInfo.isWindows) {
            actionGroup.add(new GoToDesktopAction(this));
        }
        actionGroup.add(new GoToParentFolderAction(this));
        actionGroup.addSeparator();
        actionGroup.add(new CreateNewFolderAction(this));
        actionGroup.add(new DeleteFileAndDirAction(this));
        actionGroup.add(new UploadFileAndDirAction(this));
        actionGroup.addSeparator();
        actionGroup.add(new RefreshFolderAction(this));
        actionGroup.add(new ShowHiddenFileAndDirAction(this));

        ActionToolbar toolbar = ActionManager.getInstance()
                .createActionToolbar("LocalFileContainer@bar", actionGroup, true);
        toolbar.setTargetComponent(this);

        final JPanel northPanel = new JPanel(new GridBagLayout());
        northPanel.setBorder(JBUI.Borders.empty(2, 0));

        northPanel.add(toolbar.getComponent(),new GridBagConstraints(1, 0, 1, 1, 1, 1, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));
        northPanel.add(filePath,new GridBagConstraints(0, 0, 1, 1, 0.5, 1, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));
        setToolbar(northPanel);
    }

    private void initFileTable() {
        table = new JBTable(new FileTableModel(Collections.emptyList()));
        table.getEmptyText().setText("No Data");
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        new RowDoubleClickAction(this).installOn(table);

        refreshFileList();

        setContent(new JScrollPane(table));
    }

}