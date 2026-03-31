package io.github.yueryou.easydev.plugin.model;

import com.intellij.util.xmlb.annotations.OptionTag;
import com.intellij.util.xmlb.annotations.Tag;
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
@Tag("pipeline")
public class Pipeline implements UniqueModel {

    private String id;
    private String uid;
    private String name;

    @OptionTag("steps")
    private List<PipelineStepWrapper> stepWrappers;
    private FailureStrategy onFailure;
    private long createdAt;
    private long updatedAt;

    // 缓存已转换的步骤列表，避免重复创建对象
    private transient List<PipelineStep> cachedSteps;
    // 标记缓存是否有效
    private transient boolean stepsCacheValid;

    public Pipeline() {
        this.stepWrappers = new ArrayList<>();
        this.onFailure = FailureStrategy.STOP;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.stepsCacheValid = false;
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
     * 获取步骤列表。使用缓存避免每次调用都创建新对象。
     */
    public List<PipelineStep> getSteps() {
        if (!stepsCacheValid) {
            if (stepWrappers == null) {
                cachedSteps = new ArrayList<>();
            } else {
                cachedSteps = stepWrappers.stream()
                        .filter(wrapper -> wrapper != null)
                        .map(PipelineStepWrapper::toStep)
                        .filter(step -> step != null)
                        .collect(Collectors.toList());
            }
            stepsCacheValid = true;
        }
        return cachedSteps != null ? cachedSteps : new ArrayList<>();
    }

    public void setSteps(List<PipelineStep> steps) {
        this.stepWrappers = new ArrayList<>();
        if (steps != null) {
            for (PipelineStep step : steps) {
                stepWrappers.add(new PipelineStepWrapper(step));
            }
        }
        // 标记缓存失效
        stepsCacheValid = false;
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
