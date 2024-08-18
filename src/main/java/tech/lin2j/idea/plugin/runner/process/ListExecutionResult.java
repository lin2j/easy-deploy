package tech.lin2j.idea.plugin.runner.process;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.enums.Constant;

import javax.swing.Icon;
import java.lang.reflect.Field;

/**
 * @author linjinjia
 * @date 2024/7/28 20:59
 */
public class ListExecutionResult implements ExecutionResult {
    private final ListProcessHandler processHandler;

    private final ExecutionEnvironment environment;
    private final Executor executor;

    private final UploadProcessHandler firstProcessHandler;

    public ListExecutionResult(@NotNull ListProcessHandler processHandler, ExecutionEnvironment environment) {
        this.environment = environment;
        this.executor = environment.getExecutor();
        this.processHandler = processHandler;
        this.firstProcessHandler = processHandler.getProcessHandlers().get(0);

        showRunnerTab();
    }

    @Override
    public ExecutionConsole getExecutionConsole() {
        return firstProcessHandler.getConsole();
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

    private void showRunnerTab() {
        Project project = environment.getProject();
        ToolWindowManager windowManager = ToolWindowManager.getInstance(project);
        ToolWindow runWindow = windowManager.getToolWindow("Run");
        ContentManager contentManager = runWindow.getContentManager();

        removePluginRunTab(contentManager);

        Field displayField = ReflectionUtil.getDeclaredField(RunContentDescriptor.class, "myDisplayName");
        int size = processHandler.getProcessHandlers().size();
        for (int i = 1; i < size; i++) {
            UploadProcessHandler h = processHandler.getProcessHandlers().get(i);

            RunContentDescriptor descriptor = new RunContentBuilder(h.getExecutionResult(), environment)
                    .showRunContent(null);
            descriptor.setSelectContentWhenAdded(false);
            Content content = contentManager.findContent(h.getName());

            if (content != null) {
                RunContentDescriptor oldDescriptor = content.getUserData(RunContentDescriptor.DESCRIPTOR_KEY);
                assert oldDescriptor != null;
                setDescriptorFieldValue(displayField, descriptor, h.getName());
                RunContentManager.getInstance(project).showRunContent(executor, descriptor, oldDescriptor);
            } else {
                Content newContent = createNewContent(descriptor);
                newContent.putUserData(RunContentDescriptor.DESCRIPTOR_KEY, descriptor);
                newContent.setDisplayName(h.getName());
                descriptor.setAttachedContent(newContent);
                contentManager.addContent(newContent);
            }
        }
    }

    private void removePluginRunTab(ContentManager contentManager) {
        Content[] contents = contentManager.getContents();
        for (Content content : contents) {
            boolean remove = content.getDisplayName().startsWith(Constant.RUN_TAB_PREFIX);
            if (remove) {
                contentManager.removeContent(content, true);
            }
        }

    }

    private Content createNewContent(RunContentDescriptor descriptor) {
        String processDisplayName = descriptor.getDisplayName();
        Content content =
                ContentFactory.SERVICE.getInstance().createContent(descriptor.getComponent(), processDisplayName, true);
        content.putUserData(ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
        Icon icon = descriptor.getIcon();
        content.setIcon(icon == null ? executor.getToolWindowIcon() : icon);
        return content;
    }

    private void setDescriptorFieldValue(Field field, RunContentDescriptor descriptor, Object value) {
        if (field == null) {
            return;
        }
        try {
            field.set(descriptor, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
