package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ui.ftp.FileTableContainer;

/**
 * @author linjinjia
 * @date 2024/4/4 16:19
 */
public class HomeDirectoryAction extends AnAction {

    private final FileTableContainer container;

    public HomeDirectoryAction(FileTableContainer container) {
        super("Home Directory", "Go to user home directory", AllIcons.Nodes.HomeFolder);
        this.container = container;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (StringUtil.isEmpty(container.getHomePath())) {
            return;
        }
        container.setPath(container.getHomePath());
    }
}
