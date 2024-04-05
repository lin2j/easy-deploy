package tech.lin2j.idea.plugin.action.ftp.local;

/**
 * @author linjinjia
 * @date 2024/4/4 16:20
 */

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ui.ftp.LocalFileContainer;

public class GoToDesktopAction extends AnAction {

    private final LocalFileContainer container;

    public GoToDesktopAction(LocalFileContainer container) {
        super("Desktop Directory", "Go to desktop directory", AllIcons.Nodes.Desktop);
        this.container = container;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}