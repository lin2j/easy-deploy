package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.action.NewUpdateThreadAction;
import tech.lin2j.idea.plugin.ui.ftp.container.LocalFileTableContainer;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

/**
 * @author linjinjia
 * @date 2024/4/25 22:47
 */
public class ProjectPathAction extends NewUpdateThreadAction {
    private static final String text = MessagesBundle.getText("action.ftp.project.text");
    private static final String desc = MessagesBundle.getText("action.ftp.project.description");

    public LocalFileTableContainer container;

    public ProjectPathAction(LocalFileTableContainer container) {
        super(text, desc, AllIcons.Actions.ProjectDirectory);
        this.container = container;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
       container.setPath(container.getProject().getBasePath());
    }
}