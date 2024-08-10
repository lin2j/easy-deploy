package tech.lin2j.idea.plugin.ui.ftp.container;

import com.intellij.openapi.Disposable;
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
import com.intellij.ui.TextFieldWithStoredHistory;
import com.intellij.ui.table.JBTable;
import com.intellij.util.PathUtil;
import com.intellij.util.ui.JBUI;
import org.apache.commons.collections.CollectionUtils;
import tech.lin2j.idea.plugin.action.EnterKeyAdapter;
import tech.lin2j.idea.plugin.action.SFTPTableMouseListener;
import tech.lin2j.idea.plugin.action.ftp.*;
import tech.lin2j.idea.plugin.file.TableFile;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.ui.dialog.FilePropertiesDialog;
import tech.lin2j.idea.plugin.uitl.UiUtil;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author linjinjia
 * @date 2024/4/10 22:20
 */
public abstract class AbstractFileTableContainer extends SimpleToolWindowPanel implements FileTableContainer, Disposable {

    private final Project project;
    protected boolean showHiddenFileAndDir = false;
    protected JBTable table;
    protected List<TableFile> fileList;
    protected TextFieldWithStoredHistory filePath;
    private final boolean isLocalPanel;
    private final String actionPlace;
    private final Long id;

    /**
     * At any time, a project can have at most one table in focus.
     */
    private static final Map<Project, Set<Long>> FOCUS_MAP = new ConcurrentHashMap<>();

    public AbstractFileTableContainer(boolean vertical, Project project, boolean isLocalPanel) {
        super(vertical);
        this.project = project;
        this.isLocalPanel = isLocalPanel;
        this.actionPlace = isLocalPanel ? "LocalFileContainer@bar" : "RemoteFileContainer@bar";
        this.id = new Random().nextLong();
        if (!FOCUS_MAP.containsKey(project)) {
            FOCUS_MAP.put(project, new HashSet<>());
        }
    }

    protected void init() {
        String storedKey = "ED-SFTP-PATH-" + (isLocalPanel ? "Local" : "Remote");
        filePath = new TextFieldWithStoredHistory(storedKey);
        filePath.setText(getHomePath());
        filePath.setHistorySize(ConfigHelper.pluginSetting().getHistoryPathSize());
        filePath.setMinimumAndPreferredWidth(1);
        filePath.addKeyboardListener(new EnterKeyAdapter() {
            @Override
            protected void doAction(KeyEvent e) {
                setPath(filePath.getText());
                filePath.addCurrentTextToHistory();
            }
        });
        filePath.addItemListener(e -> setPath(filePath.getText()));

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

    @Override
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
        if (SystemInfo.isWindows
                && StringUtil.isNotEmpty(parent)
                && parent.length() == 2
                && parent.matches("[A-Za-z]:")) {
            parent += "\\";
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

    @Override
    public void dispose() {
        FOCUS_MAP.get(project).remove(id);
    }

    protected void addExtractAction(DefaultActionGroup actionGroup) {

    }

    /**
     * Add a loading component to the remote panel.
     */
    protected void addTableLoadingLayer() {

    }

    protected void initToolBar() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new HomeDirectoryAction(this));
        if (isLocalPanel) {
            actionGroup.add(new ProjectPathAction((LocalFileTableContainer) this));
            if (SystemInfo.isWindows) {
                actionGroup.add(new GoToDesktopAction((LocalFileTableContainer) this));
            }
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
        table.getEmptyText().setText("No data");
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        UiUtil.hideTableLine(table);

        table.addMouseListener(new SFTPTableMouseListener(this));
        table.addKeyListener(new SpaceKeyListener());
        table.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                FOCUS_MAP.get(project).add(id);
            }

            @Override
            public void focusLost(FocusEvent e) {
                FOCUS_MAP.get(project).remove(id);
            }
        });

        new RowDoubleClickAction(this).installOn(table);
        setContent(new JScrollPane(table));
        addTableLoadingLayer();
        // async
        ApplicationManager.getApplication().executeOnPooledThread(this::refreshFileList);
    }

    protected boolean showFile(TableFile tf) {
        if (showHiddenFileAndDir) {
            return true;
        }
        return !tf.isHidden();
    }

    protected abstract TableModel getTableModel();

    private class SpaceKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int[] rows = table.getSelectedRows();
            if (!FOCUS_MAP.get(project).contains(id)
                    || rows.length != 1
                    || e.getKeyCode() != KeyEvent.VK_SPACE) {
                return;
            }

            new FilePropertiesDialog(fileList.get(rows[0])).show();
        }
    }
}