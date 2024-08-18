package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.action.NewUpdateThreadAction;
import tech.lin2j.idea.plugin.file.TableFile;
import tech.lin2j.idea.plugin.ui.dialog.FilesDeleteConfirmDialog;
import tech.lin2j.idea.plugin.ui.ftp.container.FileTableContainer;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import java.util.List;

/**
 * @author linjinjia
 * @date 2024/4/4 17:11
 */
public class DeleteFileAndDirAction extends NewUpdateThreadAction {
    private static final String text = MessagesBundle.getText("action.ftp.delete.text");
    private static final String desc = MessagesBundle.getText("action.ftp.delete.description");

    private final FileTableContainer container;

    public DeleteFileAndDirAction(FileTableContainer container) {
        super(text, desc, AllIcons.Actions.Cancel);
        this.container = container;
    }

    public DeleteFileAndDirAction(int count, FileTableContainer container) {
        this(container);
        if (count > 1) {
            String text = "Delete (" + count + " Selected)";
            getTemplatePresentation().setText(text);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        List<TableFile> selectedFiles = container.getSelectedFiles();
        if (CollectionUtils.isEmpty(selectedFiles)) {
            return;
        }

        boolean confirm = new FilesDeleteConfirmDialog(selectedFiles, null).showAndGet();
        if (!confirm) {
            return;
        }

        for (TableFile selectedFile : selectedFiles) {
            container.deleteFileAndDir(selectedFile);
        }

        container.refreshFileList();
    }
}