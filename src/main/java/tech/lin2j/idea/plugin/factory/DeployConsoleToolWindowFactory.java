package tech.lin2j.idea.plugin.factory;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.event.listener.UploadProfileSelectedListener;
import tech.lin2j.idea.plugin.module.CommandExecuteView;
import tech.lin2j.idea.plugin.module.DeployConsoleView;

/**
 * @author linjinjia
 * @date 2022/4/24 16:07
 */
public class DeployConsoleToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        // deploy tab
        DeployConsoleView deployConsoleView = new DeployConsoleView(project);
        Content deploy = contentFactory.createContent(deployConsoleView, "Deploy", false);
        toolWindow.getContentManager().addContent(deploy);
        // messages tab
        CommandExecuteView commandExecuteView = new CommandExecuteView(project);
        Content messages = contentFactory.createContent(commandExecuteView, "Messages", false);
        toolWindow.getContentManager().addContent(messages);

        // make sure every time you click on the tool window is the console
        project.getMessageBus().connect().subscribe(ToolWindowManagerListener.TOPIC, new ToolWindowManagerListener() {
            @Override
            public void stateChanged() {
                boolean messagesSelected = toolWindow.getContentManager().isSelected(messages);
                if (!toolWindow.isVisible() && messagesSelected) {
                    toolWindow.getContentManager().setSelectedContent(deploy);
                }
            }
        });

        ApplicationContext.getApplicationContext().addApplicationListener(deployConsoleView.getConsoleUi());
        ApplicationContext.getApplicationContext().addApplicationListener(new UploadProfileSelectedListener());
        ApplicationContext.getApplicationContext().addApplicationListener(commandExecuteView.getMessageUi());
    }
}