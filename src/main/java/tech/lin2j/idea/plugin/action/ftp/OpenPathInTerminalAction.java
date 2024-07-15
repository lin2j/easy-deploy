package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.action.OpenTerminalAction;
import tech.lin2j.idea.plugin.file.TableFile;
import tech.lin2j.idea.plugin.ui.ftp.container.RemoteFileTableContainer;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import java.util.List;

/**
 * @author linjinjia
 * @date 2024/7/10 22:20
 */
public class OpenPathInTerminalAction extends AnAction {

    private static final String text = MessagesBundle.getText("action.ftp.popup.open-in-ftp.text");
    private static final String desc = MessagesBundle.getText("action.ftp.popup.open-in-ftp.desc");

    private final RemoteFileTableContainer tableContainer;

    public OpenPathInTerminalAction(RemoteFileTableContainer tableContainer) {
        super(text, desc, null);
        this.tableContainer = tableContainer;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String workingDirectory = tableContainer.getPath();
        List<TableFile> files = tableContainer.getSelectedFiles();
        if (files != null && files.size() > 0 && files.get(0).isDirectory()) {
            workingDirectory = files.get(0).getFilePath();
        }
        Integer sshId = tableContainer.getSshId();
        Project project = tableContainer.getProject();

        new OpenTerminalAction(sshId, project, workingDirectory).actionPerformed(null);
    }
}