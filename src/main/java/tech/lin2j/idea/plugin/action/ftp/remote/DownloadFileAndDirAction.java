package tech.lin2j.idea.plugin.action.ftp.remote;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ui.ftp.RemoteFileContainer;

/**
 * @author linjinjia
 * @date 2024/4/4 17:24
 */
public class DownloadFileAndDirAction extends AnAction {
    private final RemoteFileContainer container;

    public DownloadFileAndDirAction(RemoteFileContainer container) {
        super("Download", "Download file and directory", AllIcons.Actions.Download);
        this.container = container;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}