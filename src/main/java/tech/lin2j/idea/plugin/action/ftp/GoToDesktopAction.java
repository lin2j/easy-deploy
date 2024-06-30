package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ui.ftp.container.LocalFileTableContainer;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

/**
 * @author linjinjia
 * @date 2024/4/4 16:20
 */
public class GoToDesktopAction extends AnAction {
    private static final String text = MessagesBundle.getText("action.ftp.desktop.text");
    private static final String desc = MessagesBundle.getText("action.ftp.desktop.description");

    private final LocalFileTableContainer container;

    public GoToDesktopAction(LocalFileTableContainer container) {
        super(text, desc, AllIcons.Nodes.Desktop);
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