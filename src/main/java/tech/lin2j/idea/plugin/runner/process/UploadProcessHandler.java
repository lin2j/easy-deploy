package tech.lin2j.idea.plugin.runner.process;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.enums.Constant;

import java.io.OutputStream;

/**
 *
 * @see com.intellij.execution.process.NopProcessHandler
 * @author linjinjia
 * @date 2024/7/28 20:42
 */
public class UploadProcessHandler extends ProcessHandler {

    private String name;
    private ConsoleView console;
    private ExecutionResult executionResult;

    public UploadProcessHandler() {
        super();
    }

    @Override
    protected void destroyProcessImpl() {
        notifyProcessTerminated(0);
    }

    @Override
    protected void detachProcessImpl() {
        notifyProcessDetached();
    }

    @Override
    public boolean detachIsDefault() {
        return false;
    }

    @Nullable
    @Override
    public OutputStream getProcessInput() {
        return null;
    }

    @Override
    public void notifyProcessTerminated(int exitCode) {
        super.notifyProcessTerminated(exitCode);
    }

    @Override
    protected void notifyProcessDetached() {
        super.notifyProcessDetached();
    }

    public ConsoleView getConsole() {
        return console;
    }

    public void setConsole(ConsoleView console) {
        this.console = console;
    }

    public ExecutionResult getExecutionResult() {
        return executionResult;
    }

    public void setExecutionResult(ExecutionResult executionResult) {
        this.executionResult = executionResult;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Constant.RUN_TAB_PREFIX + name;
    }
}
