package tech.lin2j.idea.plugin.runner;

import com.intellij.execution.process.ProcessHandler;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;

/**
 * @author linjinjia
 * @date 2024/5/2 16:18
 */
public class DeployRunProcessHandler extends ProcessHandler {


    @Override
    protected void destroyProcessImpl() {

    }

    @Override
    protected void detachProcessImpl() {

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
}