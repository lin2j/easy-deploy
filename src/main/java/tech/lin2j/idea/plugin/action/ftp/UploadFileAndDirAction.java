package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ui.ftp.LocalFileTableContainer;

/**
 * @author linjinjia
 * @date 2024/4/4 17:12
 */
public class UploadFileAndDirAction extends AnAction {

    private final LocalFileTableContainer localFileTableContainer;

    public UploadFileAndDirAction(LocalFileTableContainer localFileTableContainer) {
        super("Upload", "Upload file and directory", AllIcons.Actions.Upload);
        this.localFileTableContainer = localFileTableContainer;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}