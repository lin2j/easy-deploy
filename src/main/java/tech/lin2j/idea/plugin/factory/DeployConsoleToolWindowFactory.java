package tech.lin2j.idea.plugin.factory;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.event.listener.UploadProfileSelectedListener;
import tech.lin2j.idea.plugin.module.DeployConsoleView;

/**
 * @author linjinjia
 * @date 2022/4/24 16:07
 */
public class DeployConsoleToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        DeployConsoleView deployConsoleView = new DeployConsoleView(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(deployConsoleView, "Deploy", false);
        toolWindow.getContentManager().addContent(content);

        ApplicationContext.getApplicationContext().addApplicationListener(deployConsoleView.getConsoleUi());
        ApplicationContext.getApplicationContext().addApplicationListener(new UploadProfileSelectedListener());
    }
}