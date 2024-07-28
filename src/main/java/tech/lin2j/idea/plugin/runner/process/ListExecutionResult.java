package tech.lin2j.idea.plugin.runner.process;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.lang.reflect.Field;

/**
 *
 * @author linjinjia 
 * @date 2024/7/28 20:59
 */
public class ListExecutionResult implements ExecutionResult {
    private final ListProcessHandler processHandler;

    public ListExecutionResult(ListProcessHandler processHandler, ExecutionEnvironment environment) {
        this.processHandler = processHandler;

        ToolWindowManager windowManager = ToolWindowManager.getInstance(environment.getProject());
        ToolWindow runWindow = windowManager.getToolWindow("Run");

        int size = processHandler.getProcessHandlers().size();
        for (int i = 1; i < size; i++) {
            UploadProcessHandler h = processHandler.getProcessHandlers().get(i);

            RunContentDescriptor descriptor = new RunContentBuilder(h.getExecutionResult(), environment)
                    .showRunContent(null);
            descriptor.setSelectContentWhenAdded(false);
            try {
                // RunTab's title
                Field field = ReflectionUtil.getDeclaredField(descriptor.getClass(), "myDisplayName");
                if (field != null) {
                    field.set(descriptor, h.getName());
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            runWindow.getContentManager().addContent(createNewContent(descriptor, environment.getExecutor()));
        }

    }

    @Override
    public ExecutionConsole getExecutionConsole() {
         return processHandler.getProcessHandlers().get(0).getConsole();
    }

    @NotNull
    @Override
    public AnAction[] getActions() {
        return new AnAction[0];
    }

    @Override
    public ProcessHandler getProcessHandler() {
        return processHandler;
    }

    private static Content createNewContent(RunContentDescriptor descriptor, Executor executor) {
        String processDisplayName = descriptor.getDisplayName();
        Content content =
                ContentFactory.SERVICE.getInstance().createContent(descriptor.getComponent(), processDisplayName, true);
        content.putUserData(ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
        Icon icon = descriptor.getIcon();
        content.setIcon(icon == null ? executor.getToolWindowIcon() : icon);
        return content;
    }
}
