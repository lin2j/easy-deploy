package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.icons.AllIcons;
import tech.lin2j.idea.plugin.file.TableFile;
import tech.lin2j.idea.plugin.ui.dialog.FilePropertiesDialog;
import tech.lin2j.idea.plugin.ui.ftp.container.FileTableContainer;

/**
 *
 * @author linjinjia
 * @date 2024/5/24 21:43
 */
public class FilePropertiesAction extends FileAction {

    public FilePropertiesAction(FileTableContainer container) {
        super("File Properties", container);
        getTemplatePresentation().setIcon(AllIcons.Actions.Properties);
    }

    @Override
    protected void handle(TableFile file) {
        new FilePropertiesDialog(file).show();
    }
}
