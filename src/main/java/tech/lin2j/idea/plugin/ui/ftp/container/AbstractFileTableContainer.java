package tech.lin2j.idea.plugin.ui.ftp.container;

import com.intellij.ide.browsers.DefaultBrowserPolicy;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.CollectionComboBoxModel;
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
import tech.lin2j.idea.plugin.file.TableFile;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        filePath.setColumns(1);
        filePath.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setPath(filePath.getText());
            }
        });

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
        if (!"/".equals(path) && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    @Override
    public String getParentPath() {
        String cur = getPath();
        // windows
        if (SystemInfo.isWindows) {
            // root
            if (StringUtil.isNotEmpty(cur)
                    && cur.length() == 3
                    && cur.matches("[A-Za-z]:\\\\")) {
                return cur;
            }
        }
        String parent = PathUtil.getParentPath(cur);
        if (SystemInfo.isUnix && StringUtil.isEmpty(parent)) {
            parent = "/";
        }

        return parent;
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
        int gridx = 0;
        northPanel.setBorder(JBUI.Borders.empty(2, 0));
        northPanel.add(toolbar.getComponent(), new GridBagConstraints(gridx, 0, 1, 1, 0, 1, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));
        gridx++;
        if (isLocalPanel && SystemInfo.isWindows) {
            List<String> roots = Arrays.stream(File.listRoots()).map(File::toString).collect(Collectors.toList());
            ComboBox<String> fileSystemRoots = new ComboBox<>(new CollectionComboBoxModel<>(roots));
            fileSystemRoots.addItemListener(e -> {
                String root = Objects.toString(fileSystemRoots.getSelectedItem());
                setPath(root);
            });
            northPanel.add(fileSystemRoots, new GridBagConstraints(gridx, 0, 1, 1, 0, 1, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                    JBUI.emptyInsets(), 0, 0));
            gridx++;
        }
        northPanel.add(filePath, new GridBagConstraints(gridx, 0, 1, 1, 1, 1, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));
        setToolbar(northPanel);
    }

    protected void initFileTable() {
        table = new JBTable(getTableModel());
        table.getEmptyText().setText("No Data");
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        new RowDoubleClickAction(this).installOn(table);

        // async
        ApplicationManager.getApplication().executeOnPooledThread(this::refreshFileList);

        setContent(new JScrollPane(table));
    }

    protected boolean showFile(TableFile tf) {
        if (showHiddenFileAndDir) return true;
        return !tf.isHidden();
    }

    protected abstract TableModel getTableModel();
}