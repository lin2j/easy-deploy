package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ui.ftp.container.LocalFileTableContainer;

/**
 * @author linjinjia
 * @date 2024/4/25 22:47
 */
public class ProjectPathAction extends AnAction {

    public LocalFileTableContainer container;

    public ProjectPathAction(LocalFileTableContainer container) {
        super("Project Path", "Go to project path", AllIcons.Actions.ProjectDirectory);
        this.container = container;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
       container.setPath(container.getProject().getBasePath());
    }
}