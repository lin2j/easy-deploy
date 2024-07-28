package tech.lin2j.idea.plugin.runner.process;

import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.util.List;

/**
 *
 * @author linjinjia
 * @date 2024/7/28 20:42
 */
public class ListProcessHandler extends ProcessHandler {
    private final List<UploadProcessHandler> processHandlers;

    public ListProcessHandler(List<UploadProcessHandler> processHandlers) {
        this.processHandlers = processHandlers;
    }

    @Override
    public void notifyProcessTerminated(int exitCode) {
        this.processHandlers.forEach((h) -> {
            h.notifyProcessTerminated(exitCode);
        });
    }

    @Override
    public void startNotify() {
        this.processHandlers.forEach(ProcessHandler::startNotify);
    }

    @Override
    public boolean waitFor() {
        return this.processHandlers.stream()
                .allMatch(ProcessHandler::waitFor);
    }

    @Override
    public boolean waitFor(long timeoutInMilliseconds) {
        return this.processHandlers.stream()
                .allMatch((h) -> h.waitFor(timeoutInMilliseconds));
    }

    @Override
    public void destroyProcess() {
        this.processHandlers.forEach(ProcessHandler::destroyProcess);
    }

    @Override
    public void detachProcess() {
        this.processHandlers.forEach(ProcessHandler::detachProcess);
    }

    @Override
    public boolean isProcessTerminated() {
        return this.processHandlers.stream()
                .allMatch(ProcessHandler::isProcessTerminated);
    }

    @Override
    public boolean isProcessTerminating() {
        return this.processHandlers.stream()
                .anyMatch(ProcessHandler::isProcessTerminating);
    }

    @Override
    public @Nullable Integer getExitCode() {
        boolean allZero = this.processHandlers.stream()
                .map(ProcessHandler::getExitCode)
                .allMatch((i) -> i != null && i.equals(0));
        return allZero ? 0 : 1;
    }

    @Override
    public void addProcessListener(ProcessListener listener) {
        this.processHandlers.forEach((h) -> {
            h.addProcessListener(listener);
        });
    }

    @Override
    public void addProcessListener(@NotNull ProcessListener listener, @NotNull Disposable parentDisposable) {
        this.processHandlers.forEach((h) -> {
            h.addProcessListener(listener, parentDisposable);
        });
    }

    @Override
    public void removeProcessListener(ProcessListener listener) {
        this.processHandlers.forEach((h) -> {
            h.removeProcessListener(listener);
        });
    }

    @Override
    protected void notifyProcessDetached() {
        this.processHandlers.forEach(UploadProcessHandler::notifyProcessDetached);
    }

    @Override
    public void notifyTextAvailable(@NotNull String text, @NotNull Key outputType) {
        this.processHandlers.forEach((h) -> {
            h.notifyTextAvailable(text, outputType);
        });
    }

    @Override
    public boolean isStartNotified() {
        return this.processHandlers.stream().allMatch(ProcessHandler::isStartNotified);
    }

    @Override
    protected void destroyProcessImpl() {
        this.processHandlers.forEach(UploadProcessHandler::destroyProcessImpl);
    }

    @Override
    protected void detachProcessImpl() {
        this.processHandlers.forEach(UploadProcessHandler::detachProcessImpl);
    }

    @Override
    public boolean detachIsDefault() {
        return false;
    }

    @Override
    public @Nullable OutputStream getProcessInput() {
        return null;
    }

    public List<UploadProcessHandler> getProcessHandlers() {
        return this.processHandlers;
    }
}
