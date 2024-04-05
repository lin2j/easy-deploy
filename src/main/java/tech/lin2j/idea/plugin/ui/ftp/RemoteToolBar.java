package tech.lin2j.idea.plugin.ui.ftp;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.file.FTPFile;

/**
 * @author linjinjia
 * @date 2024/4/2 22:30
 */
public class RemoteToolBar {

    private final JBList<FTPFile> fileList;

    public RemoteToolBar(JBList<FTPFile> fileList) {
        this.fileList = fileList;
    }

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
        return new AnAction[] {backAction(), forwardAction(), refreshAction()};
    }
}