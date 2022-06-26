package tech.lin2j.idea.plugin.ssh;

import com.intellij.execution.process.ProcessHandler;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;

/**
 * @author linjinjia
 * @date 2022/6/25 00:38
 */
public class SshProcessHandler extends ProcessHandler {

    private final CustomTtyConnector ttyConnector;

    public SshProcessHandler(CustomTtyConnector ttyConnector) {
        this.ttyConnector = ttyConnector;
    }

    @Override
    protected void destroyProcessImpl() {
        this.notifyProcessDetached();
    }

    @Override
    protected void detachProcessImpl() {
        this.notifyProcessDetached();
    }

    @Override
    public boolean detachIsDefault() {
        return false;
    }

    @Override
    public @Nullable OutputStream getProcessInput() {
        return ttyConnector.getOutputStream();
    }
}