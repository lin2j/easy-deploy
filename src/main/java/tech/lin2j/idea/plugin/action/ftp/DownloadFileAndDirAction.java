package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.model.event.FileTransferEvent;
import tech.lin2j.idea.plugin.enums.TransferEventType;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.ui.ftp.container.RemoteFileTableContainer;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

/**
 * @author linjinjia
 * @date 2024/4/4 17:24
 */
public class DownloadFileAndDirAction extends AnAction {
    private static final String text = MessagesBundle.getText("action.ftp.download.text");
    private static final String desc = MessagesBundle.getText("action.ftp.download.description");

    private final RemoteFileTableContainer container;

    public DownloadFileAndDirAction(RemoteFileTableContainer container) {
        super(text, desc, AllIcons.Actions.Download);
        this.container = container;
    }

    public DownloadFileAndDirAction(int count, RemoteFileTableContainer container) {
        this(container);
        if (count > 1) {
            String text = "Download (" + count + " Selected)";
            getTemplatePresentation().setText(text);
        }
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        FileTransferEvent event = new FileTransferEvent(container, false, TransferEventType.START);
        ApplicationContext.getApplicationContext().publishEvent(event);
    }
}