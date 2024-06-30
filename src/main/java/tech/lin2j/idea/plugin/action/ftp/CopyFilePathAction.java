package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.util.ui.TextTransferable;
import tech.lin2j.idea.plugin.file.TableFile;
import tech.lin2j.idea.plugin.ui.ftp.container.FileTableContainer;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

/**
 *
 * @author linjinjia
 * @date 2024/5/24 21:42
 */
public class CopyFilePathAction extends FileAction {
    private static final String text = MessagesBundle.getText("action.ftp.popup.copy.path.text");

    public CopyFilePathAction(FileTableContainer container) {
        super(text, container);
    }


    @Override
    protected void handle(TableFile file) {
        String path = file.getFilePath();
        CopyPasteManager.getInstance().setContents(new TextTransferable(path));
    }
}
