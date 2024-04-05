package tech.lin2j.idea.plugin.action.ftp.local;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ui.ftp.LocalFileContainer;

/**
 * @author linjinjia
 * @date 2024/4/4 16:40
 */
public class GoToParentFolderAction extends AnAction {

    private final LocalFileContainer container;

    public GoToParentFolderAction(LocalFileContainer container) {
        super("Parent Folder", "Go to parent folder", AllIcons.Actions.Rollback);
        this.container = container;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String parent = PathUtil.getParentPath(container.getPath());
        container.setPath(parent);
    }
}
