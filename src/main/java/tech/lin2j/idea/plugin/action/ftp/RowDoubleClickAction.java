package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.ui.DoubleClickListener;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.PluginSetting;
import tech.lin2j.idea.plugin.domain.model.event.FileTransferEvent;
import tech.lin2j.idea.plugin.enums.SFTPAction;
import tech.lin2j.idea.plugin.enums.TransferEventType;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.file.TableFile;
import tech.lin2j.idea.plugin.ui.dialog.FilePropertiesDialog;
import tech.lin2j.idea.plugin.ui.ftp.container.FileTableContainer;

import java.awt.event.MouseEvent;

/**
 * @author linjinjia
 * @date 2024/4/6 14:03
 */
public class RowDoubleClickAction extends DoubleClickListener {

    private final PluginSetting setting = ConfigHelper.pluginSetting();

    private final FileTableContainer container;

    public RowDoubleClickAction(FileTableContainer container) {
        this.container = container;
    }

    @Override
    protected boolean onDoubleClick(MouseEvent e) {
        int selected = container.getTable().getSelectedRow();
        if (selected == -1) {
            return false;
        }
        TableFile tf = container.getFileList().get(selected);
        if (!tf.isDirectory()) {
            SFTPAction action = setting.getDoubleClickAction();
            if (action == SFTPAction.Properties) {
                new FilePropertiesDialog(tf).show();
            } else if (action == SFTPAction.Transfer) {
                boolean isUpload = container.isLocal();
                FileTransferEvent event = new FileTransferEvent(container, isUpload, TransferEventType.START);
                ApplicationContext.getApplicationContext().publishEvent(event);
            }
            return true;
        }

        container.setPath(tf.getFilePath());
        return true;
    }
}