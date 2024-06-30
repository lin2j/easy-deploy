package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.file.RemoteTableFile;
import tech.lin2j.idea.plugin.file.TableFile;
import tech.lin2j.idea.plugin.ui.dialog.ChangePermissionsDialog;
import tech.lin2j.idea.plugin.ui.ftp.container.RemoteFileTableContainer;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import java.awt.MediaTracker;
import java.util.List;

/**
 * @author linjinjia
 * @date 2024/4/4 17:24
 */
public class ChangePermissionAction extends AnAction {
    private static final String text = MessagesBundle.getText("action.ftp.popup.permission.text");

    private final RemoteFileTableContainer container;

    public ChangePermissionAction(RemoteFileTableContainer container) {
        super(text, "Change permission of file and directory", AllIcons.Actions.ChangeView);
        this.container = container;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
         List<TableFile> fileList = container.getSelectedFiles();
         if (CollectionUtils.isNotEmpty(fileList)) {
             RemoteTableFile file = (RemoteTableFile) fileList.get(0);
             boolean refresh = new ChangePermissionsDialog(file, container.getFTPClient()).showAndGet();
             if (refresh) {
                 container.refreshFileList();
             }
         }
    }
}