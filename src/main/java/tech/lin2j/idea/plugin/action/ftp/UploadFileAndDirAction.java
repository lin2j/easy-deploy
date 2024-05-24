package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.domain.model.event.FileTransferEvent;
import tech.lin2j.idea.plugin.enums.TransferEventType;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.ui.ftp.container.LocalFileTableContainer;

/**
 * @author linjinjia
 * @date 2024/4/4 17:12
 */
public class UploadFileAndDirAction extends AnAction {

    private LocalFileTableContainer localContainer;

    public UploadFileAndDirAction(LocalFileTableContainer localContainer) {
        super("Upload", "Upload file and directory", AllIcons.Actions.Upload);
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