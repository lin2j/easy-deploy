package tech.lin2j.idea.plugin.action.ftp.remote;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ui.ftp.RemoteFileContainer;

/**
 * @author linjinjia
 * @date 2024/4/4 17:11
 */
public class DeleteFileAndDirAction extends AnAction {
    private final RemoteFileContainer container;


    public DeleteFileAndDirAction(RemoteFileContainer container) {
        super("Delete", "Delete folder or file", AllIcons.Actions.Cancel);
        this.container = container;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}