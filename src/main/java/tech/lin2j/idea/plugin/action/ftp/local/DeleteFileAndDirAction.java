package tech.lin2j.idea.plugin.action.ftp.local;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ui.ftp.LocalFileContainer;

/**
 * @author linjinjia
 * @date 2024/4/4 17:11
 */
public class DeleteFileAndDirAction extends AnAction {
    private final LocalFileContainer localFileContainer;


    public DeleteFileAndDirAction(LocalFileContainer localFileContainer) {
        super("Delete", "Delete folder or file", AllIcons.Actions.Cancel);
        this.localFileContainer = localFileContainer;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}