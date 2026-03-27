package io.github.yueryou.easydev.plugin.model;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 步骤执行结果类
 */
public class StepResult {
    private boolean success;
    private int exitCode;
    private String stdout;
    private String stderr;
    private String errorMessage;
    private Duration duration;
    private Map<String, Object> outputs = new HashMap<>();

    public StepResult() {
    }

    private StepResult(boolean success, String stdout, int exitCode, String errorMessage) {
        this.success = success;
        this.stdout = stdout;
        this.exitCode = exitCode;
        this.errorMessage = errorMessage;
    }

    public static StepResult success(String stdout, int exitCode) {
        return new StepResult(true, stdout, exitCode, null);
    }

    public static StepResult failure(String errorMessage) {
        return new StepResult(false, null, -1, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Map<String, Object> getOutputs() {
        return outputs;
    }

    public void setOutputs(Map<String, Object> outputs) {
        this.outputs = outputs;
    }

    public void addOutput(String key, Object value) {
        this.outputs.put(key, value);
    }

    public Object getOutput(String key) {
        return this.outputs.get(key);
    }
}
