package tech.lin2j.idea.plugin.ui.ftp;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.table.IconTableCellRenderer;
import org.apache.commons.lang.time.DateFormatUtils;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.action.ftp.local.CreateNewFolderAction;
import tech.lin2j.idea.plugin.action.ftp.local.DeleteFileAndDirAction;
import tech.lin2j.idea.plugin.action.ftp.local.GoToDesktopAction;
import tech.lin2j.idea.plugin.action.ftp.local.GoToParentFolderAction;
import tech.lin2j.idea.plugin.action.ftp.local.HomeDirectoryAction;
import tech.lin2j.idea.plugin.action.ftp.local.RefreshFolderAction;
import tech.lin2j.idea.plugin.action.ftp.local.ShowHiddenFileAndDirAction;
import tech.lin2j.idea.plugin.action.ftp.local.UploadFileAndDirAction;
import tech.lin2j.idea.plugin.uitl.IconUtil;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author linjinjia
 * @date 2024/4/4 10:19
 */
public class LocalFileContainer extends SimpleToolWindowPanel {

    private JBTextField filePath;
    private JBList<VirtualFile> fileList;
    private JTable table;

    private static final String[] columnNames = {"Name", "Size", "Type", "Modified"};

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

    public void refreshFileList() {
        File file = new File(filePath.getText());
        if (!file.exists() || !file.isDirectory()) {
            Messages.showErrorDialog("Specified path not found.", "Path");
            return;
        }
        VirtualFile vFile = LocalFileSystem.getInstance().findFileByIoFile(file);
        if (vFile != null) {
            List<VirtualFile> children = Arrays.stream(vFile.getChildren())
                    .sorted(Comparator.comparing(VirtualFile::getName)).collect(Collectors.toList());
            Object[][] data = new Object[children.size()][4];
            for (int i = 0; i < children.size(); i++) {
                VirtualFile vf = children.get(i);
                data[i][0] = vf.getName();
                data[i][1] = StringUtil.formatFileSize(vf.getLength());
                data[i][2] = vf.isDirectory() ? "Folder" : vf.getFileType().getName();
                String modified = DateFormatUtils.format(vf.getTimeStamp(), "yyyy-MM-dd HH:mm:ss");
                data[i][3] = modified;
            }

            table.setModel(new DefaultTableModel(data, columnNames));
        }
    }

    private void init() {
        filePath = new JBTextField();
        fileList = new JBList<>();
        table = new JBTable();
        initToolBar();
        initFileList();
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

    private void initFileList() {
        filePath.setText(System.getProperty("user.home"));

        Object[][] data = new Object[0][4];

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
        table.setModel(tableModel);
        table.setFocusable(false);
        table.setRowSelectionAllowed(false);
        table.setFillsViewportHeight(true);
        table.setRowHeight(30);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        TableColumn typeColumn = table.getColumn("Name");
        typeColumn.setCellRenderer(new IconTableCellRenderer<Icon>() {
            @NotNull
            @Override
            protected Icon getIcon(@NotNull Icon value, JTable table, int row) {
                return value;
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
                super.getTableCellRendererComponent(table, value, selected, focus, row, column);
                setText("");
                return this;
            }

            @Override
            protected boolean isCenterAlignment() {
                return true;
            }
        });

        refreshFileList();

        setContent(new JScrollPane(table));
    }

    /**
     * Local JBList cell renderer
     */
    private static class LocalListCellRenderer extends DefaultListCellRenderer {
        @NotNull
        @Override
        public Component getListCellRendererComponent(@NotNull JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            VirtualFile vFile = (VirtualFile) value;
            Icon icon = IconUtil.getIcon(vFile);
            setText(vFile.getName());
            setIcon(icon);
            return this;
        }
    }
}