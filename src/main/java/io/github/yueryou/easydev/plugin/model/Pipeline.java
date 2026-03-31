package io.github.yueryou.easydev.plugin.model;

import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.util.xmlb.annotations.OptionTag;
import com.intellij.util.xmlb.annotations.Tag;
import org.jdom.Element;
import tech.lin2j.idea.plugin.model.UniqueModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 流水线配置类
 *
 * 注意：steps 字段存储多态类型（LocalCommandStep, UploadStep, RemoteCommandStep）
 * 使用 JSON 序列化步骤列表以避免 XMLB 多态问题
 */
@Tag("pipeline")
public class Pipeline implements UniqueModel, JDOMExternalizable {

    private String id;
    private String uid;
    private String name;

    @OptionTag("steps")
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
    public void writeExternal(Element element) {
        // 使用 XMLB 序列化基本字段
        Element dataElement = new Element("data");
        dataElement.setAttribute("id", id != null ? id : "");
        dataElement.setAttribute("uid", uid != null ? uid : "");
        dataElement.setAttribute("name", name != null ? name : "");
        dataElement.setAttribute("onFailure", onFailure != null ? onFailure.name() : "STOP");
        dataElement.setAttribute("createdAt", String.valueOf(createdAt));
        dataElement.setAttribute("updatedAt", String.valueOf(updatedAt));

        // 手动序列化步骤列表，使用类型标识
        if (steps != null && !steps.isEmpty()) {
            Element stepsElement = new Element("steps");
            for (PipelineStep step : steps) {
                Element stepElement = new Element("step");
                stepElement.setAttribute("type", step.getType().name());
                stepElement.setAttribute("uid", step.getUid() != null ? step.getUid() : "");
                stepElement.setAttribute("name", step.getName() != null ? step.getName() : "");
                stepElement.setAttribute("enabled", String.valueOf(step.isEnabled()));

                // 根据类型序列化特定字段
                if (step instanceof LocalCommandStep) {
                    LocalCommandStep localStep = (LocalCommandStep) step;
                    stepElement.setAttribute("command", localStep.getCommand() != null ? localStep.getCommand() : "");
                    stepElement.setAttribute("workingDir", localStep.getWorkingDir() != null ? localStep.getWorkingDir() : "");
                    stepElement.setAttribute("timeout", String.valueOf(localStep.getTimeout()));
                    stepElement.setAttribute("commandId", localStep.getCommandId() != null ? localStep.getCommandId() : "");
                } else if (step instanceof UploadStep) {
                    UploadStep uploadStep = (UploadStep) step;
                    stepElement.setAttribute("uploadProfileId", uploadStep.getUploadProfileId() != null ? uploadStep.getUploadProfileId() : "");
                    stepElement.setAttribute("serverId", uploadStep.getServerId() != null ? uploadStep.getServerId() : "");
                    stepElement.setAttribute("createRemoteDir", String.valueOf(uploadStep.isCreateRemoteDir()));
                } else if (step instanceof RemoteCommandStep) {
                    RemoteCommandStep remoteStep = (RemoteCommandStep) step;
                    stepElement.setAttribute("command", remoteStep.getCommand() != null ? remoteStep.getCommand() : "");
                    stepElement.setAttribute("workingDir", remoteStep.getWorkingDir() != null ? remoteStep.getWorkingDir() : "");
                    stepElement.setAttribute("commandId", remoteStep.getCommandId() != null ? remoteStep.getCommandId() : "");
                    stepElement.setAttribute("serverId", remoteStep.getServerId() != null ? remoteStep.getServerId() : "");
                }
                stepsElement.addContent(stepElement);
            }
            dataElement.addContent(stepsElement);
        }
        element.addContent(dataElement);
    }

    @Override
    public void readExternal(Element element) {
        Element dataElement = element.getChild("data");
        if (dataElement != null) {
            id = dataElement.getAttributeValue("id");
            uid = dataElement.getAttributeValue("uid");
            name = dataElement.getAttributeValue("name");
            String onFailureStr = dataElement.getAttributeValue("onFailure");
            if (onFailureStr != null) {
                onFailure = FailureStrategy.valueOf(onFailureStr);
            }
            String createdAtStr = dataElement.getAttributeValue("createdAt");
            if (createdAtStr != null) {
                createdAt = Long.parseLong(createdAtStr);
            }
            String updatedAtStr = dataElement.getAttributeValue("updatedAt");
            if (updatedAtStr != null) {
                updatedAt = Long.parseLong(updatedAtStr);
            }

            // 反序列化步骤列表
            steps = new ArrayList<>();
            Element stepsElement = dataElement.getChild("steps");
            if (stepsElement != null) {
                for (Element stepElement : stepsElement.getChildren("step")) {
                    String type = stepElement.getAttributeValue("type");
                    PipelineStep step = createStepByType(type);
                    if (step != null) {
                        step.setUid(stepElement.getAttributeValue("uid"));
                        step.setName(stepElement.getAttributeValue("name"));
                        step.setEnabled(Boolean.parseBoolean(stepElement.getAttributeValue("enabled")));

                        if (step instanceof LocalCommandStep) {
                            LocalCommandStep localStep = (LocalCommandStep) step;
                            localStep.setCommand(stepElement.getAttributeValue("command"));
                            localStep.setWorkingDir(stepElement.getAttributeValue("workingDir"));
                            localStep.setTimeout(Integer.parseInt(stepElement.getAttributeValue("timeout")));
                            localStep.setCommandId(stepElement.getAttributeValue("commandId"));
                        } else if (step instanceof UploadStep) {
                            UploadStep uploadStep = (UploadStep) step;
                            uploadStep.setUploadProfileId(stepElement.getAttributeValue("uploadProfileId"));
                            uploadStep.setServerId(stepElement.getAttributeValue("serverId"));
                            uploadStep.setCreateRemoteDir(Boolean.parseBoolean(stepElement.getAttributeValue("createRemoteDir")));
                        } else if (step instanceof RemoteCommandStep) {
                            RemoteCommandStep remoteStep = (RemoteCommandStep) step;
                            remoteStep.setCommand(stepElement.getAttributeValue("command"));
                            remoteStep.setWorkingDir(stepElement.getAttributeValue("workingDir"));
                            remoteStep.setCommandId(stepElement.getAttributeValue("commandId"));
                            remoteStep.setServerId(stepElement.getAttributeValue("serverId"));
                        }
                        steps.add(step);
                    }
                }
            }
        }
    }

    private PipelineStep createStepByType(String type) {
        if (type == null) return null;
        switch (type) {
            case "LOCAL_COMMAND": return new LocalCommandStep();
            case "UPLOAD": return new UploadStep();
            case "REMOTE_COMMAND": return new RemoteCommandStep();
            default: return null;
        }
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
