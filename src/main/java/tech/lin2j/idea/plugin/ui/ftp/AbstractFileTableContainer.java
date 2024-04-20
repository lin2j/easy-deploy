package tech.lin2j.idea.plugin.ui.ftp;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.intellij.util.PathUtil;
import com.intellij.util.ui.JBUI;
import org.apache.commons.collections.CollectionUtils;
import tech.lin2j.idea.plugin.action.ftp.CreateNewFolderAction;
import tech.lin2j.idea.plugin.action.ftp.DeleteFileAndDirAction;
import tech.lin2j.idea.plugin.action.ftp.DownloadFileAndDirAction;
import tech.lin2j.idea.plugin.action.ftp.GoToDesktopAction;
import tech.lin2j.idea.plugin.action.ftp.GoToParentFolderAction;
import tech.lin2j.idea.plugin.action.ftp.HomeDirectoryAction;
import tech.lin2j.idea.plugin.action.ftp.RefreshFolderAction;
import tech.lin2j.idea.plugin.action.ftp.RowDoubleClickAction;
import tech.lin2j.idea.plugin.action.ftp.ShowHiddenFileAndDirAction;
import tech.lin2j.idea.plugin.action.ftp.UploadFileAndDirAction;
import tech.lin2j.idea.plugin.event.ApplicationListener;
import tech.lin2j.idea.plugin.file.TableFile;
import tech.lin2j.idea.plugin.ui.table.FileTableModel;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author linjinjia
 * @date 2024/4/10 22:20
 */
public abstract class AbstractFileTableContainer extends SimpleToolWindowPanel implements FileTableContainer {

    private final Project project;
    protected boolean showHiddenFileAndDir = false;
    protected JBTable table;
    protected List<TableFile> fileList;
    protected JBTextField filePath;
    private final boolean isLocalPanel;
    private final String actionPlace;

    public AbstractFileTableContainer(boolean vertical, Project project, boolean isLocalPanel) {
        super(vertical);
        this.project = project;
        this.isLocalPanel = isLocalPanel;
        this.actionPlace = isLocalPanel ? "LocalFileContainer@bar" : "RemoteFileContainer@bar";
    }

    protected void init() {
        filePath = new JBTextField();
        filePath.setText(getHomePath());

        initToolBar();
        initFileTable();
    }

    @Override
    public JBTable getTable() {
        return table;
    }

    @Override
    public List<TableFile> getFileList() {
        return fileList;
    }

    @Override
    public List<TableFile> getSelectedFiles() {
        if (CollectionUtils.isEmpty(fileList)) {
            return Collections.emptyList();
        }
        int[] rows = table.getSelectedRows();
        List<TableFile> selectedFiles = new ArrayList<>();
        for (int idx : rows) {
            if (idx < fileList.size()) {
                selectedFiles.add(fileList.get(idx));
            }
        }
        return selectedFiles;
    }

    @Override
    public void reversedHiddenFlag() {
        showHiddenFileAndDir = !showHiddenFileAndDir;
    }

    @Override
    public boolean showHiddenFileAndDir() {
        return showHiddenFileAndDir;
    }

    public String getPath() {
        String path = filePath.getText();
        if (StringUtil.isEmpty(path)) {
            return path;
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    @Override
    public String getParentPath() {
        return PathUtil.getParentPath(getPath());
    }

    @Override
    public void setPath(String path) {
        filePath.setText(path);
        refreshFileList();
    }

    @Override
    public Project getProject() {
        return project;
    }

    protected void addExtractAction(DefaultActionGroup actionGroup) {

    }

    protected void initToolBar() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new HomeDirectoryAction(this));
        if (isLocalPanel && SystemInfo.isWindows) {
            actionGroup.add(new GoToDesktopAction((LocalFileTableContainer) this));
        }
        actionGroup.add(new GoToParentFolderAction(this));
        actionGroup.addSeparator();
        actionGroup.add(new CreateNewFolderAction(this));
        actionGroup.add(new DeleteFileAndDirAction(this));
        if (isLocalPanel) {
            actionGroup.add(new UploadFileAndDirAction((LocalFileTableContainer) this));
        } else {
            actionGroup.add(new DownloadFileAndDirAction((RemoteFileTableContainer) this));
        }
        actionGroup.addSeparator();
        actionGroup.add(new RefreshFolderAction(this));
        actionGroup.add(new ShowHiddenFileAndDirAction(this));

        addExtractAction(actionGroup);

        ActionToolbar toolbar = ActionManager.getInstance()
                .createActionToolbar(actionPlace, actionGroup, true);
        toolbar.setTargetComponent(this);

        final JPanel northPanel = new JPanel(new GridBagLayout());
        northPanel.setBorder(JBUI.Borders.empty(2, 0));

        northPanel.add(toolbar.getComponent(), new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));
        northPanel.add(filePath, new GridBagConstraints(1, 0, 1, 1, 1, 1, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));
        setToolbar(northPanel);
    }

    protected void initFileTable() {
        table = new JBTable(new FileTableModel(Collections.emptyList()));
        table.getEmptyText().setText("No Data");
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        new RowDoubleClickAction(this).installOn(table);

        refreshFileList();

        setContent(new JScrollPane(table));
    }

    protected boolean showFile(TableFile tf) {
        if (showHiddenFileAndDir) return true;
        return !tf.isHidden();
    }
}