package tech.lin2j.idea.plugin.action.ftp.local;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ui.ftp.LocalFileContainer;

/**
 * @author linjinjia
 * @date 2024/4/4 16:19
 */
public class HomeDirectoryAction extends AnAction {

    private final LocalFileContainer localFileContainer;

    public HomeDirectoryAction(LocalFileContainer localFileContainer) {
        super("Home Directory", "Go to user home directory", AllIcons.Nodes.HomeFolder);
        this.localFileContainer = localFileContainer;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}
