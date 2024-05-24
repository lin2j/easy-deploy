package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.util.ui.TextTransferable;
import tech.lin2j.idea.plugin.file.TableFile;
import tech.lin2j.idea.plugin.ui.ftp.container.FileTableContainer;

/**
 *
 * @author linjinjia
 * @date 2024/5/24 21:42
 */
public class CopyFilePathAction extends FileAction {

    public CopyFilePathAction(FileTableContainer container) {
        super("Copy Path", container);
    }


    @Override
    protected void handle(TableFile file) {
        String path = file.getFilePath();
        CopyPasteManager.getInstance().setContents(new TextTransferable(path));
    }
}
