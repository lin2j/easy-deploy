package tech.lin2j.idea.plugin.ui.ftp;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author linjinjia
 * @date 2024/4/2 22:29
 */
public class LocalToolBar {

    private final JBList<VirtualFile> fileList;
    private final TextFieldWithBrowseButton localPath;

    public LocalToolBar(JBList<VirtualFile> fileList, TextFieldWithBrowseButton localPath) {
        this.fileList = fileList;
        this.localPath = localPath;
    }

    /**
     * If the selected file is a directory, enter the directory; otherwise, take no action.
     *
     * @return action
     */
    public AnAction forwardAction() {
        return new AnAction(AllIcons.Actions.Forward) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                System.out.println("local forward");
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                VirtualFile vFile = fileList.getSelectedValue();
                if (vFile != null) {
                    e.getPresentation().setEnabled(vFile.isDirectory());
                }
            }
        };
    }

    /**
     * If the current directory is not the top-level directory, you can navigate up one level.
     *
     * @return action
     */
    public AnAction backAction() {
        return new AnAction(AllIcons.Actions.Back) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                System.out.println("local back");
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                VirtualFile vFile = LocalFileSystem.getInstance().findFileByIoFile(new File(localPath.getText()));
                if (vFile != null) {
                    VirtualFile parent = vFile.getParent();
                    e.getPresentation().setEnabled(parent != null && parent.exists());
                }
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
        return new AnAction[]{backAction(), forwardAction(), refreshAction()};
    }
}