package tech.lin2j.idea.plugin.ui.ftp;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ActionManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.table.JBTable;
import net.schmizz.sshj.sftp.SFTPClient;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ssh.SshConnectionManager;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.uitl.IconUtil;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author linjinjia
 * @date 2024/3/31 00:57
 */
public class FTPConsoleUi {
    private JPanel mainPanel;
    private TextFieldWithBrowseButton localPath;
    private JBTable outputTable;
    private JBList<VirtualFile> localFileList;
    private JBList<FTPFile> remoteFileList;
    private JScrollPane localFileScrollPane;
    private JScrollPane remoteFileScrollPane;
    private JPanel localToolbar;
    private JPanel remoteToolbar;
    private JTextField remotePath;

    private final Project project;
    private final SshServer server;
    private final SFTPClient sftpClient;

    private final String[] columnNames = {"Name", "State", "Progress", "Size", "Local", "Remote"};

    public FTPConsoleUi(Project project, SshServer server) throws IOException {
        this.project = project;
        this.server = server;
        this.localPath.setText(System.getProperty("user.home"));
        sftpClient = SshConnectionManager.makeSshClient(server).newSFTPClient();
        initUi();
    }

    public void initUi() throws IOException{
        Toolkit tk = Toolkit.getDefaultToolkit();
        mainPanel.setMinimumSize(new Dimension(tk.getScreenSize().width / 3, 0));

        final ActionManagerEx mgr = (ActionManagerEx) ActionManager.getInstance();
        // local toolbar
        ActionToolbar localActionToolbar = mgr.createActionToolbar("ToolbarDecorator",
                new DefaultActionGroup(new LocalToolBar().actions()), true);
        localToolbar.add(localActionToolbar.getComponent());
        // remote toolbar
        ActionToolbar remoteActionToolbar = mgr.createActionToolbar("ToolbarDecorator",
                new DefaultActionGroup(new LocalToolBar().actions()), true);
        remoteToolbar.add(remoteActionToolbar.getComponent());

        VirtualFile vFile = LocalFileSystem.getInstance().findFileByIoFile(new File(this.localPath.getText()));
        localFileList.setModel(new CollectionListModel<>(vFile.getChildren()));
        localFileList.setCellRenderer(new LocalListCellRenderer());
        localFileList.setEnabled(true);

        remoteFileList.setModel(new CollectionListModel<>(getRemoteFile()));
        remoteFileList.setCellRenderer(new RemoteListCellRenderer());
        remoteFileList.setEnabled(true);

        Object[][] data = new Object[0][6];

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
        outputTable.setModel(tableModel);
        outputTable.setFocusable(false);
        outputTable.setRowSelectionAllowed(false);
        outputTable.setFillsViewportHeight(true);
        outputTable.setRowHeight(30);
        outputTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    public List<FTPFile> getRemoteFile() throws IOException {
         return sftpClient.ls("/root").stream().map(FTPFile::new).collect(Collectors.toList());
    }

    public JPanel getMainPanel() {
        return mainPanel;
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

    /**
     * Local JBList cell renderer
     */
    private static class RemoteListCellRenderer extends DefaultListCellRenderer {
        @NotNull
        @Override
        public Component getListCellRendererComponent(@NotNull JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            FTPFile vFile = (FTPFile) value;
            setText(vFile.getName());
            setIcon(vFile.getIcon());
            return this;
        }
    }

    /**
     * Local file action toolbar
     */
    private static class LocalToolBar {

        /**
         * If the selected file is a directory, enter the directory; otherwise, take no action.
         * @return action
         */
        public AnAction forwardAction() {
            return new AnAction(AllIcons.Actions.Forward) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    System.out.println("local forward");
                }
            };
        }

        /**
         * If the current directory is not the top-level directory, you can navigate up one level.
         * @return action
         */
        public AnAction backAction() {
            return new AnAction(AllIcons.Actions.Back) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    System.out.println("local back");
                }
            };
        }

        /**
         * Refresh the file list in the current path.
         *
         * @return action
         */
        public AnAction refreshAction() {
            return new AnAction(AllIcons.Actions.Refresh) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    System.out.println("local refresh");
                }
            };
        }

        public AnAction[] actions() {
            LocalToolBar localToolBar = new LocalToolBar();
            return new AnAction[] {localToolBar.backAction(), localToolBar.forwardAction(), refreshAction()};
        }
    }
}