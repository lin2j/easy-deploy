package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.action.NewUpdateThreadAction;
import tech.lin2j.idea.plugin.ui.ftp.container.FileTableContainer;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

/**
 * @author linjinjia
 * @date 2024/4/4 16:40
 */
public class GoToParentFolderAction extends NewUpdateThreadAction {
    private static final String text = MessagesBundle.getText("action.ftp.parent.text");
    private static final String desc = MessagesBundle.getText("action.ftp.parent.description");


    private final FileTableContainer container;

    public GoToParentFolderAction(FileTableContainer container) {
        super(text, desc, AllIcons.Actions.Rollback);
        this.container = container;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        container.setPath(container.getParentPath());
    }
}
