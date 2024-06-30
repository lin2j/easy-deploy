package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.util.ui.TextTransferable;
import tech.lin2j.idea.plugin.file.TableFile;
import tech.lin2j.idea.plugin.ui.ftp.container.FileTableContainer;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

/**
 *
 * @author linjinjia 
 * @date 2024/5/24 23:28
 */
public class CopyFileNameAction extends FileAction {
    private static final String text = MessagesBundle.getText("action.ftp.popup.copy.filename.text");

    public CopyFileNameAction(FileTableContainer container) {
        super(text, container);
    }

    @Override
    protected void handle(TableFile file) {
        String data = file.getName();
        CopyPasteManager.getInstance().setContents(new TextTransferable(data));
    }
}
