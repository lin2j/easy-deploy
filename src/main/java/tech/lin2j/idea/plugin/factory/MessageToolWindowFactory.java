package tech.lin2j.idea.plugin.factory;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.module.CommandExecuteView;

/**
 * @author linjinjia
 * @date 2022/4/27 15:44
 */
public class MessageToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        CommandExecuteView view = new CommandExecuteView(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(view, "Messages", false);
        toolWindow.getContentManager().addContent(content);

        ApplicationContext.getApplicationContext().addApplicationListener(view.getMessageUi());
    }
}