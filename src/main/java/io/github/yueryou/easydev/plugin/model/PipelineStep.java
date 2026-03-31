package io.github.yueryou.easydev.plugin.model;

import com.intellij.util.xmlb.annotations.Tag;
import tech.lin2j.idea.plugin.model.UniqueModel;

/**
 * 流水线步骤抽象基类
 */
@Tag("step")
public abstract class PipelineStep implements UniqueModel {

    private String uid;
    private String name;
    protected StepType type;
    private boolean enabled;

    public PipelineStep() {
    }

    public PipelineStep(String name, StepType type) {
        this.name = name;
        this.type = type;
        this.enabled = true;
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StepType getType() {
        return type;
    }

    public void setType(StepType type) {
        this.type = type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
