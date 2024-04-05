package tech.lin2j.idea.plugin.action.ftp.remote;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ui.ftp.RemoteFileContainer;

/**
 * @author linjinjia
 * @date 2024/4/4 16:40
 */
public class GoToParentFolderAction extends AnAction {

    private final RemoteFileContainer container;

    public GoToParentFolderAction(RemoteFileContainer container) {
        super("Parent Folder", "Go to parent folder", AllIcons.Actions.Rollback);
        this.container = container;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}
