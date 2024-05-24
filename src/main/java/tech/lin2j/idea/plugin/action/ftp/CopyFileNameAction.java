package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.util.ui.TextTransferable;
import tech.lin2j.idea.plugin.file.TableFile;
import tech.lin2j.idea.plugin.ui.ftp.container.FileTableContainer;

/**
 *
 * @author linjinjia 
 * @date 2024/5/24 23:28
 */
public class CopyFileNameAction extends FileAction {

    public CopyFileNameAction(FileTableContainer container) {
        super("Copy File Name", container);
    }

    @Override
    protected void handle(TableFile file) {
        String data = file.getName();
        CopyPasteManager.getInstance().setContents(new TextTransferable(data));
    }
}
