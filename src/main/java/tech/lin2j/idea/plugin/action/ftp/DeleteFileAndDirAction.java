package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.io.FileUtil;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.file.TableFile;
import tech.lin2j.idea.plugin.ui.dialog.FilesDeleteConfirmDialog;
import tech.lin2j.idea.plugin.ui.ftp.FileTableContainer;

import java.io.File;
import java.util.List;

/**
 * @author linjinjia
 * @date 2024/4/4 17:11
 */
public class DeleteFileAndDirAction extends AnAction {
    private final FileTableContainer container;


    public DeleteFileAndDirAction(FileTableContainer container) {
        super("Delete", "Delete folder or file", AllIcons.Actions.Cancel);
        this.container = container;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        List<TableFile> selectedFiles = container.getSelectedFiles();
        if (CollectionUtils.isEmpty(selectedFiles)) {
            return;
        }

        boolean confirm = new FilesDeleteConfirmDialog(selectedFiles).showAndGet();
        if (!confirm) {
            return;
        }

        for (TableFile selectedFile : selectedFiles) {
            container.deleteFileAndDir(selectedFile);
        }

        container.refreshFileList();
    }
}