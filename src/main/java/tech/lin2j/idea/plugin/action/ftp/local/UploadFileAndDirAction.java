package tech.lin2j.idea.plugin.action.ftp.local;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ui.ftp.LocalFileContainer;

/**
 * @author linjinjia
 * @date 2024/4/4 17:12
 */
public class UploadFileAndDirAction extends AnAction {

    private final LocalFileContainer localFileContainer;

    public UploadFileAndDirAction(LocalFileContainer localFileContainer) {
        super("Upload", "Upload file and directory", AllIcons.Actions.Upload);
        this.localFileContainer =  localFileContainer;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}