package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ui.ftp.container.FileTableContainer;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

/**
 * @author linjinjia
 * @date 2024/4/4 17:13
 */
public class RefreshFolderAction extends AnAction {
    private static final String text = MessagesBundle.getText("action.ftp.refresh.text");
    private static final String desc = MessagesBundle.getText("action.ftp.refresh.description");

    private final FileTableContainer container;

    public RefreshFolderAction(FileTableContainer container) {
        super(text, desc, AllIcons.Actions.Refresh);
        this.container = container;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        container.refreshFileList();
    }
}