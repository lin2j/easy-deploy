package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.model.event.FileTransferEvent;
import tech.lin2j.idea.plugin.enums.TransferEventType;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.ui.ftp.container.LocalFileTableContainer;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

/**
 * @author linjinjia
 * @date 2024/4/4 17:12
 */
public class UploadFileAndDirAction extends AnAction {
    private static final String text = MessagesBundle.getText("action.ftp.upload.text");
    private static final String desc = MessagesBundle.getText("action.ftp.upload.description");

    private final LocalFileTableContainer localContainer;

    public UploadFileAndDirAction(LocalFileTableContainer localContainer) {
        super(text, desc, AllIcons.Actions.Upload);
        this.localContainer = localContainer;
    }

    public UploadFileAndDirAction(int count, LocalFileTableContainer localContainer) {
        this(localContainer);
        if (count > 1) {
            String text = "Upload (" + count + " Selected)";
            getTemplatePresentation().setText(text);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        FileTransferEvent event = new FileTransferEvent(localContainer, true, TransferEventType.START);
        ApplicationContext.getApplicationContext().publishEvent(event);
    }
}