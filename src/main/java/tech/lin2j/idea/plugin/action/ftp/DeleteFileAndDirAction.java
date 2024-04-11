package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ui.ftp.FileTableContainer;

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


        // 删除前确认
//        Messages.showConfirmationDialog(container, "")

        // 最后再执行删除
        container.deleteFileAndDir();
    }
}