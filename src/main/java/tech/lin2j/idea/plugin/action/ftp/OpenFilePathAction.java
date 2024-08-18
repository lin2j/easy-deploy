package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.action.NewUpdateThreadAction;
import tech.lin2j.idea.plugin.file.TableFile;
import tech.lin2j.idea.plugin.ui.ftp.container.FileTableContainer;
import tech.lin2j.idea.plugin.uitl.FileUtil;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author linjinjia 
 * @date 2024/7/15 22:37
 */
public class OpenFilePathAction extends NewUpdateThreadAction {

    private static final String winText = MessagesBundle.getText("action.ftp.popup.open-in-win.text");
    private static final String macText = MessagesBundle.getText("action.ftp.popup.open-in-mac.text");
    private static final String text = SystemInfo.isMac ? macText : winText;
    private static final String desc = MessagesBundle.getText("action.ftp.popup.open-in.desc");
    private static final String errorTitle = MessagesBundle.getText("action.ftp.popup.open-in.error.title");
    private static final String errorTip = MessagesBundle.getText("action.ftp.popup.open-in.error.tip");

    private final FileTableContainer tableContainer;

    public OpenFilePathAction(FileTableContainer tableContainer) {
        super(text, desc, null);
        this.tableContainer = tableContainer;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String targetPath = tableContainer.getPath();
        try {
            List<TableFile> files = tableContainer.getSelectedFiles();
           if (files != null && files.size() > 0 && files.get(0).isDirectory()) {
               targetPath = files.get(0).getFilePath();
            }
            FileUtil.openDir(targetPath);
        } catch (IOException ex) {
            SwingUtilities.invokeLater(() -> Messages.showErrorDialog(errorTip + ex.getMessage(), errorTitle));
        }
    }
}
