package tech.lin2j.idea.plugin.action.ftp.local;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ui.ftp.LocalFileContainer;

/**
 * @author linjinjia
 * @date 2024/4/4 17:09
 */
public class CreateNewFolderAction extends AnAction {

    private final LocalFileContainer localFileContainer;


    public CreateNewFolderAction(LocalFileContainer localFileContainer) {
        super("New Directory", "Create new folder", AllIcons.Actions.NewFolder);
        this.localFileContainer = localFileContainer;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        // 当前路径为非目录时，不可用
    }
}