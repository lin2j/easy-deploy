package io.github.yueryou.easydev.plugin.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 流水线执行结果类
 */
public class PipelineResult {
    private boolean success;
    private PipelineStep failedStep;
    private List<PipelineStep> skippedSteps = new ArrayList<>();
    private Map<PipelineStep, StepResult> stepResults = new LinkedHashMap<>();

    public PipelineResult() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public PipelineStep getFailedStep() {
        return failedStep;
    }

    public void setFailedStep(PipelineStep failedStep) {
        this.failedStep = failedStep;
    }

    public List<PipelineStep> getSkippedSteps() {
        return skippedSteps;
    }

    public void setSkippedSteps(List<PipelineStep> skippedSteps) {
        this.skippedSteps = skippedSteps;
    }

    public void addSkippedStep(PipelineStep step) {
        this.skippedSteps.add(step);
    }

    public Map<PipelineStep, StepResult> getStepResults() {
        return stepResults;
    }

    public void setStepResults(Map<PipelineStep, StepResult> stepResults) {
        this.stepResults = stepResults;
    }

    public void addStepResult(PipelineStep step, StepResult result) {
        this.stepResults.put(step, result);
    }

    public StepResult getStepResult(PipelineStep step) {
        return this.stepResults.get(step);
    }
}
