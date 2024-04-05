package tech.lin2j.idea.plugin.ui.ftp;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.JBUI;
import net.schmizz.sshj.sftp.SFTPClient;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.action.ftp.remote.CreateNewFolderAction;
import tech.lin2j.idea.plugin.action.ftp.remote.DeleteFileAndDirAction;
import tech.lin2j.idea.plugin.action.ftp.remote.DownloadFileAndDirAction;
import tech.lin2j.idea.plugin.action.ftp.remote.GoToParentFolderAction;
import tech.lin2j.idea.plugin.action.ftp.remote.HomeDirectoryAction;
import tech.lin2j.idea.plugin.action.ftp.remote.RefreshFolderAction;
import tech.lin2j.idea.plugin.action.ftp.remote.ShowHiddenFileAndDirAction;
import tech.lin2j.idea.plugin.file.FTPFile;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author linjinjia
 * @date 2024/4/4 10:19
 */
public class RemoteFileContainer extends SimpleToolWindowPanel {

    private JTextField filePath;
    private JBList<FTPFile> fileList;
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
    }

    private void init() {
        filePath = new JTextField();
        fileList = new JBList<>();
        initToolBar();
        initFileList();
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

    private void initFileList() {
        try {
            List<FTPFile> files = sftpClient.ls("/root").stream().map(FTPFile::new).collect(Collectors.toList());
            fileList.setModel(new CollectionListModel<>(files));
            fileList.setCellRenderer(new LocalListCellRenderer());
            fileList.setEnabled(true);
            setContent(new JScrollPane(fileList));
        } catch (Exception e) {

        }
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
            FTPFile vFile = (FTPFile) value;
            setText(vFile.getName());
            setIcon(vFile.getIcon());
            return this;
        }
    }
}