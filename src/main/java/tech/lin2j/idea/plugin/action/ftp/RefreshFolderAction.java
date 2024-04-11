package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ui.ftp.FileTableContainer;

/**
 * @author linjinjia
 * @date 2024/4/4 17:13
 */
public class RefreshFolderAction extends AnAction {
    private final FileTableContainer container;

    public RefreshFolderAction(FileTableContainer container) {
        super("Refresh", "Refresh folder", AllIcons.Actions.Refresh);
        this.container = container;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        container.refreshFileList();
    }
}