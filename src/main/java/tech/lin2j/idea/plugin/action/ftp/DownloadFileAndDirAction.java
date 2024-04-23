package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.domain.model.event.FileTransferEvent;
import tech.lin2j.idea.plugin.enums.TransferEventType;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.ui.ftp.container.RemoteFileTableContainer;

/**
 * @author linjinjia
 * @date 2024/4/4 17:24
 */
public class DownloadFileAndDirAction extends AnAction {
    private final RemoteFileTableContainer container;

    public DownloadFileAndDirAction(RemoteFileTableContainer container) {
        super("Download", "Download file and directory", AllIcons.Actions.Download);
        this.container = container;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        FileTransferEvent event = new FileTransferEvent(container, false, TransferEventType.START);
        ApplicationContext.getApplicationContext().publishEvent(event);
    }
}