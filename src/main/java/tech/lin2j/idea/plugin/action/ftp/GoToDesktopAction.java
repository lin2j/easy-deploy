package tech.lin2j.idea.plugin.action.ftp;

/**
 * @author linjinjia
 * @date 2024/4/4 16:20
 */

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ui.ftp.container.LocalFileTableContainer;

public class GoToDesktopAction extends AnAction {

    private final LocalFileTableContainer container;

    public GoToDesktopAction(LocalFileTableContainer container) {
        super("Desktop Directory", "Go to desktop directory", AllIcons.Nodes.Desktop);
        this.container = container;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        VirtualFile desktop = container.getDesktopDirectory();
        if (desktop != null) {
            container.setPath(desktop.getPath());
        }
    }
}