package tech.lin2j.idea.plugin.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.ui.table.JBTable;
import tech.lin2j.idea.plugin.action.ftp.*;
import tech.lin2j.idea.plugin.ui.ftp.container.FileTableContainer;
import tech.lin2j.idea.plugin.ui.ftp.container.LocalFileTableContainer;
import tech.lin2j.idea.plugin.ui.ftp.container.RemoteFileTableContainer;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * @author linjinjia 
 * @date 2024/5/24 23:24
 */
public class SFTPTableMouseListener extends MouseAdapter {

    private final JBTable table;
    private final FileTableContainer container;

    public SFTPTableMouseListener(FileTableContainer container) {
        this.table = container.getTable();
        this.container = container;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        invokePopMenu(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        invokePopMenu(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        invokePopMenu(e);
    }

    private void invokePopMenu(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return;
        }
        DefaultActionGroup actionGroup = new DefaultActionGroup();

        //  copy action
        DefaultActionGroup copyActionGroup = new DefaultActionGroup("Copy", true);
        copyActionGroup.getTemplatePresentation().setIcon(AllIcons.Actions.Copy);
        copyActionGroup.add(new CopyFilePathAction(container));
        copyActionGroup.add(new CopyFileNameAction(container));

        // file action
        DefaultActionGroup fileActionGroup = new DefaultActionGroup();
        fileActionGroup.add(copyActionGroup);
        int count = table.getSelectedRowCount();
        if (container.isLocal()) {
            fileActionGroup.add(new UploadFileAndDirAction(count,  (LocalFileTableContainer) container));
        } else {
            fileActionGroup.add(new DownloadFileAndDirAction(count, (RemoteFileTableContainer) container));
        }
        fileActionGroup.add(new DeleteFileAndDirAction(count, container));
        fileActionGroup.add(new FilePropertiesAction(container));

        // other action
        DefaultActionGroup commonActionGroup = new DefaultActionGroup();
        commonActionGroup.add(new CreateNewFolderAction(container));
        commonActionGroup.add(new RefreshFolderAction(container));

        // join
        actionGroup.addAll(fileActionGroup);
        actionGroup.addSeparator();
        actionGroup.addAll(commonActionGroup);

        JPopupMenu component = ActionManager.getInstance().createActionPopupMenu("right", actionGroup).getComponent();
        component.show(e.getComponent(), e.getX(), e.getY());
    }
}
