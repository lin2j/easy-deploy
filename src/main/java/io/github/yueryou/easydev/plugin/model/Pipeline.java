package io.github.yueryou.easydev.plugin.model;

import com.intellij.util.xmlb.annotations.OptionTag;
import com.intellij.util.xmlb.annotations.Tag;
import tech.lin2j.idea.plugin.model.UniqueModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 流水线配置类
 *
 * 注意：steps 字段存储多态类型（LocalCommandStep, UploadStep, RemoteCommandStep）
 * 每个子类都有 @Tag 注解用于 XML 序列化
 */
@Tag("pipeline")
public class Pipeline implements UniqueModel {

    private String id;
    private String uid;
    private String name;

    @OptionTag("steps")
    @Tag("step")
    private List<PipelineStep> steps;
    private FailureStrategy onFailure;
    private long createdAt;
    private long updatedAt;

    public Pipeline() {
        this.steps = new ArrayList<>();
        this.onFailure = FailureStrategy.STOP;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
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

    public List<PipelineStep> getSteps() {
        // 过滤 null 元素（可能由于 XML 反序列化失败导致）
        if (steps == null) {
            return new ArrayList<>();
        }
        return steps.stream()
                .filter(step -> step != null)
                .toList();
    }

    public void setSteps(List<PipelineStep> steps) {
        this.steps = steps;
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
