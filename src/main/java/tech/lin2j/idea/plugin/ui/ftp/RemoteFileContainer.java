package tech.lin2j.idea.plugin.ui.ftp;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import tech.lin2j.idea.plugin.action.ftp.RowDoubleClickAction;
import tech.lin2j.idea.plugin.action.ftp.remote.CreateNewFolderAction;
import tech.lin2j.idea.plugin.action.ftp.remote.DeleteFileAndDirAction;
import tech.lin2j.idea.plugin.action.ftp.remote.DownloadFileAndDirAction;
import tech.lin2j.idea.plugin.action.ftp.remote.GoToParentFolderAction;
import tech.lin2j.idea.plugin.action.ftp.remote.HomeDirectoryAction;
import tech.lin2j.idea.plugin.action.ftp.remote.RefreshFolderAction;
import tech.lin2j.idea.plugin.action.ftp.remote.ShowHiddenFileAndDirAction;
import tech.lin2j.idea.plugin.file.RemoteTableFile;
import tech.lin2j.idea.plugin.file.TableFile;
import tech.lin2j.idea.plugin.ui.table.FileNameCellRenderer;
import tech.lin2j.idea.plugin.ui.table.FileTableModel;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author linjinjia
 * @date 2024/4/4 10:19
 */
public class RemoteFileContainer extends SimpleToolWindowPanel implements FileTableContainer {

    private static final int NAME_COLUMN = 0;

    private JTextField filePath;
    private List<TableFile> fileList;
    private JBTable table;
    private final Project project;
    private final SFTPClient sftpClient;

    public RemoteFileContainer(Project project, SFTPClient sftpClient) {
        super(true);
        this.sftpClient = sftpClient;
        this.project = project;

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

    private void refreshFileList() {
        try {
            fileList = sftpClient.ls("/root").stream()
                    .sorted(Comparator.comparing(RemoteResourceInfo::getName))
                    .map(RemoteTableFile::new)
                    .filter(f -> !f.isHidden())
                    .collect(Collectors.toList());

            FileTableModel tableModel = new FileTableModel(fileList);
            table.setModel(tableModel);

            TableColumn nameColumn = table.getColumnModel().getColumn(NAME_COLUMN);
            nameColumn.setCellRenderer(new FileNameCellRenderer());
        } catch (Exception e) {

        }
    }

    private void init() {
        filePath = new JTextField();
        initToolBar();
        initFileTable();
    }

    private void initToolBar() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new HomeDirectoryAction(this));
        actionGroup.add(new GoToParentFolderAction(this));
        actionGroup.addSeparator();
        actionGroup.add(new CreateNewFolderAction(this));
        actionGroup.add(new DeleteFileAndDirAction(this));
        actionGroup.add(new DownloadFileAndDirAction(this));
        actionGroup.addSeparator();
        actionGroup.add(new RefreshFolderAction(this));
        actionGroup.add(new ShowHiddenFileAndDirAction(this));

        ActionToolbar toolbar = ActionManager.getInstance()
                .createActionToolbar("RemoteFileContainer@bar", actionGroup, true);
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