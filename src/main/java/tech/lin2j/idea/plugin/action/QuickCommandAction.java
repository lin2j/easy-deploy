package tech.lin2j.idea.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import tech.lin2j.idea.plugin.ui.dialog.CommandManageDialog;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

/**
 * 快捷指令功能入口
 */
public class QuickCommandAction extends AnAction {

    /**
     * Creates a new action with its text, description and icon set to {@code null}.
     */
    public QuickCommandAction() {
        super(MessagesBundle.getText("dialog.command.quick.frame"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        new CommandManageDialog(e.getProject()).show();
    }
}
