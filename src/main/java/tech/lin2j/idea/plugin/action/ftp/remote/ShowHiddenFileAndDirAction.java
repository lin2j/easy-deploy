package tech.lin2j.idea.plugin.action.ftp.remote;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ui.ftp.RemoteFileContainer;

/**
 * @author linjinjia
 * @date 2024/4/4 17:14
 */
public class ShowHiddenFileAndDirAction extends AnAction {
    private final RemoteFileContainer container;

    public ShowHiddenFileAndDirAction(RemoteFileContainer container) {
        super("Show Files And Directories", "Show files and directories", AllIcons.Actions.ShowHiddens);
        this.container = container;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
    }
}