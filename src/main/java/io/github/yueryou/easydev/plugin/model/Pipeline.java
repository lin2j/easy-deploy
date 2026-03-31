package io.github.yueryou.easydev.plugin.model;

import tech.lin2j.idea.plugin.model.UniqueModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 流水线配置类
 *
 * 注意：steps 字段存储多态类型（LocalCommandStep, UploadStep, RemoteCommandStep）
 * 使用 PipelineStepWrapper 进行 XmlSerializer 序列化
 */
public class Pipeline implements UniqueModel {

    private String id;
    private String uid;
    private String name;

    // 存储步骤的包装列表，用于 XmlSerializer 序列化
    private List<PipelineStepWrapper> steps;
    private FailureStrategy onFailure;
    private long createdAt;
    private long updatedAt;

    // 缓存已转换的步骤列表，避免重复创建对象
    private transient List<PipelineStep> cachedPipelineSteps;
    // 标记缓存是否有效
    private transient boolean pipelineStepsCacheValid;

    public Pipeline() {
        this.steps = new ArrayList<>();
        this.onFailure = FailureStrategy.STOP;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.pipelineStepsCacheValid = false;
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取步骤列表（供 XmlSerializer 序列化/反序列化使用）。
     */
    public List<PipelineStepWrapper> getSteps() {
        return steps;
    }

    /**
     * 设置步骤列表（供 XmlSerializer 序列化/反序列化使用）。
     */
    public void setSteps(List<PipelineStepWrapper> steps) {
        this.steps = steps;
        // 标记缓存失效
        this.pipelineStepsCacheValid = false;
    }

    /**
     * 获取转换后的 PipelineStep 列表（供业务逻辑使用）。
     * 使用缓存避免每次调用都创建新对象。
     */
    public List<PipelineStep> getPipelineSteps() {
        if (!pipelineStepsCacheValid) {
            if (steps == null) {
                cachedPipelineSteps = new ArrayList<>();
            } else {
                cachedPipelineSteps = steps.stream()
                        .filter(wrapper -> wrapper != null)
                        .map(PipelineStepWrapper::toStep)
                        .filter(step -> step != null)
                        .collect(Collectors.toList());
            }
            pipelineStepsCacheValid = true;
        }
        return cachedPipelineSteps != null ? cachedPipelineSteps : new ArrayList<>();
    }

    /**
     * 设置 PipelineStep 列表（供业务逻辑使用）。
     * 会自动转换为 PipelineStepWrapper 存储。
     */
    public void setPipelineSteps(List<PipelineStep> pipelineSteps) {
        this.steps = new ArrayList<>();
        if (pipelineSteps != null) {
            for (PipelineStep step : pipelineSteps) {
                steps.add(new PipelineStepWrapper(step));
            }
        }
        // 标记缓存失效
        this.pipelineStepsCacheValid = false;
    }

    public FailureStrategy getOnFailure() {
        return onFailure;
    }

    public void setOnFailure(FailureStrategy onFailure) {
        this.onFailure = onFailure;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pipeline pipeline = (Pipeline) o;
        if (id != null && pipeline.id != null) {
            return Objects.equals(id, pipeline.id);
        }
        return Objects.equals(uid, pipeline.uid);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(uid);
    }
}
