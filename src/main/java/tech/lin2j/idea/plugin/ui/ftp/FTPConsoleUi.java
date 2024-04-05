package tech.lin2j.idea.plugin.ui.ftp;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
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
import tech.lin2j.idea.plugin.file.FTPFile;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
                new DefaultActionGroup(new LocalToolBar(localFileList, localPath).actions()), true);
        localToolbar.add(localActionToolbar.getComponent());
        // remote toolbar
        ActionToolbar remoteActionToolbar = mgr.createActionToolbar("ToolbarDecorator",
                new DefaultActionGroup(new RemoteToolBar(remoteFileList).actions()), true);
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

    private static class LocalPathActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {

        }
    }

    private static class LocalPathEnterKeyListener extends KeyAdapter {

        @Override
        public void keyTyped(KeyEvent e) {
            System.out.println("typed");
        }

        @Override
        public void keyPressed(KeyEvent e) {
            System.out.println(e);
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                System.out.println("enter");
            }
        }
    }
}