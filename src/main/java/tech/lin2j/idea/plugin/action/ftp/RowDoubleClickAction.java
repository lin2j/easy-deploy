package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.ui.DoubleClickListener;
import tech.lin2j.idea.plugin.file.TableFile;
import tech.lin2j.idea.plugin.ui.ftp.container.FileTableContainer;

import java.awt.event.MouseEvent;

/**
 * @author linjinjia
 * @date 2024/4/6 14:03
 */
public class RowDoubleClickAction extends DoubleClickListener {

    private final FileTableContainer container;

    public RowDoubleClickAction(FileTableContainer container) {
        this.container = container;
    }

    @Override
    protected boolean onDoubleClick(MouseEvent event) {
        int selected = container.getTable().getSelectedRow();
        TableFile tf = container.getFileList().get(selected);
        if (!tf.isDirectory()) {
            return false;
        }

        container.setPath(tf.getFilePath());
        return true;
    }
}