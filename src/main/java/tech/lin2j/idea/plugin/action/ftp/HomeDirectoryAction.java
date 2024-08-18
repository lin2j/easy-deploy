package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.action.NewUpdateThreadAction;
import tech.lin2j.idea.plugin.ui.ftp.container.FileTableContainer;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

/**
 * @author linjinjia
 * @date 2024/4/4 16:19
 */
public class HomeDirectoryAction extends NewUpdateThreadAction {
    private static final String text = MessagesBundle.getText("action.ftp.home.text");
    private static final String desc = MessagesBundle.getText("action.ftp.home.description");

    private final FileTableContainer container;

    public HomeDirectoryAction(FileTableContainer container) {
        super(text, desc, AllIcons.Nodes.HomeFolder);
        this.container = container;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (StringUtil.isEmpty(container.getHomePath())) {
            return;
        }
        container.setPath(container.getHomePath());
    }
}
