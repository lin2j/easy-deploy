# Pipeline Step Server Configuration Design

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 Server 参数从 Pipeline 级别移动到 Upload 和 RemoteCommand 步骤级别，支持每个步骤独立配置 Server。

**Architecture:**
- 移除 Pipeline 类的 serverId 字段
- 在 UploadStep 和 RemoteCommandStep 类中添加 serverId 字段
- 修改 UI 编辑对话框，在步骤级别配置 Server
- 修改执行器，从步骤对象获取 Server 配置
- 提供数据迁移逻辑，自动将现有 Pipeline 的 serverId 迁移到步骤

**Tech Stack:**
- Java Swing (UI)
- IntelliJ Platform SDK
- JSON 持久化 (PipelineConfigPersistence)

---

## 1. 数据模型变更

### 1.1 Pipeline.java

移除 `serverId` 字段及相关方法：

```java
public class Pipeline implements UniqueModel {
    private String id;
    private String uid;
    private String name;
    // REMOVED: private String serverId;
    private List<PipelineStep> steps;
    private FailureStrategy onFailure;
    private long createdAt;
    private long updatedAt;

    // REMOVED: getServerId(), setServerId()
    // REMOVED: 构造函数中的 serverId 参数
}
```

### 1.2 UploadStep.java

新增 `serverId` 字段：

```java
public class UploadStep extends PipelineStep {
    private String uploadProfileId;
    private String serverId;  // NEW
    private boolean createRemoteDir;

    // NEW getters/setters
    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }
}
```

### 1.3 RemoteCommandStep.java

新增 `serverId` 字段：

```java
public class RemoteCommandStep extends PipelineStep {
    private String command;
    private String workingDir;
    private String commandId;
    private String serverId;  // NEW

    // NEW getters/setters
    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }
}
```

### 1.4 LocalCommandStep.java

无需修改（本地执行，不需要 Server）。

---

## 2. UI 变更

### 2.1 PipelineEditDialog.java

**移除：**
- 顶层 Server 选择框 (`serverComboBox`)
- Server 相关验证逻辑

**修改：**
- `doOKAction()`: 移除 Server 验证

### 2.2 StepEditDialog.java

**Upload 面板：**
- 添加 Server 选择下拉框 (`serverComboBox`)
- 填充可用 Server 列表

**RemoteCommand 面板：**
- 添加 Server 选择下拉框 (`serverComboBox`)
- 填充可用 Server 列表

**数据填充：**
- 编辑现有步骤时，从步骤的 `serverId` 字段填充选中值
- 保存时，将选中的 `serverId` 设置到步骤对象

---

## 3. 执行逻辑变更

### 3.1 PipelineExecutor.java

修改 `executeStep()` 方法，从步骤获取 Server：

```java
private static StepResult executeStep(PipelineStep step, ExecutionContext context) {
    switch (step.getType()) {
        case LOCAL_COMMAND:
            return LocalCommandExecutor.execute(step, context);
        case UPLOAD:
            UploadStep uploadStep = (UploadStep) step;
            if (uploadStep.getServerId() != null) {
                SshServer uploadServer = ConfigHelper.getSshServerById(
                    Integer.parseInt(uploadStep.getServerId())
                );
                return UploadExecutor.execute(step, context, uploadServer);
            } else {
                return StepResult.failure("Upload 步骤未配置 Server");
            }
        case REMOTE_COMMAND:
            RemoteCommandStep remoteStep = (RemoteCommandStep) step;
            if (remoteStep.getServerId() != null) {
                SshServer remoteServer = ConfigHelper.getSshServerById(
                    Integer.parseInt(remoteStep.getServerId())
                );
                return RemoteCommandExecutor.execute(step, context, remoteServer);
            } else {
                return StepResult.failure("RemoteCommand 步骤未配置 Server");
            }
        default:
            return StepResult.failure("不支持的步骤类型：" + step.getType());
    }
}
```

### 3.2 UploadExecutor.java / RemoteCommandExecutor.java

可能需要修改方法签名，接受 `SshServer` 参数。

### 3.3 ExecutionContext.java

保留 `SshServer server` 字段用于向后兼容，但逐步改为从步骤获取。

---

## 4. 数据迁移

### 4.1 PipelineConfigPersistence.java

添加数据迁移逻辑：

```java
public static List<Pipeline> getAllPipelines() {
    List<Pipeline> pipelines = loadPipelines();
    for (Pipeline pipeline : pipelines) {
        migratePipeline(pipeline);
    }
    return pipelines;
}

private static void migratePipeline(Pipeline pipeline) {
    // 如果 Pipeline 有 serverId，迁移到所有 UPLOAD/REMOTE_COMMAND 步骤
    if (pipeline.getServerId() != null) {
        for (PipelineStep step : pipeline.getSteps()) {
            if (step instanceof UploadStep) {
                ((UploadStep) step).setServerId(pipeline.getServerId());
            } else if (step instanceof RemoteCommandStep) {
                ((RemoteCommandStep) step).setServerId(pipeline.getServerId());
            }
        }
        // 清除 Pipeline 的 serverId
        pipeline.setServerId(null);
    }
}
```

---

## 5. 错误处理

### 5.1 Server 验证

**执行时验证：**
- 步骤执行时检查 `serverId` 是否为 null
- 检查 Server 配置是否存在
- 失败时记录到日志并返回错误结果

### 5.2 UI 提示

**StepEditDialog:**
- 保存时验证 Server 是否已选择
- 未选择时显示错误提示

---

## 6. 测试场景

1. **纯本地流水线**：不包含 Server 配置，正常执行
2. **单服务器流水线**：所有步骤使用同一 Server
3. **多服务器流水线**：不同步骤使用不同 Server
4. **数据迁移**：加载旧配置后，Server 自动迁移到步骤
5. **Server 缺失**：步骤配置的 Server 不存在时，报错并停止

---

## 7. 影响范围

| 文件 | 变更类型 | 说明 |
|------|----------|------|
| `Pipeline.java` | 移除字段 | 删除 serverId |
| `UploadStep.java` | 新增字段 | 添加 serverId |
| `RemoteCommandStep.java` | 新增字段 | 添加 serverId |
| `PipelineEditDialog.java` | 简化 | 移除 Server UI |
| `StepEditDialog.java` | 扩展 | 添加 Server UI |
| `PipelineExecutor.java` | 修改逻辑 | 从步骤获取 Server |
| `UploadExecutor.java` | 修改签名 | 接受 SshServer 参数 |
| `RemoteCommandExecutor.java` | 修改签名 | 接受 SshServer 参数 |
| `PipelineConfigPersistence.java` | 新增方法 | 数据迁移逻辑 |
| `messages_en.properties` | 新增 key | Step Server 标签 |
| `messages_zh.properties` | 新增 key | Step Server 标签 |

---

## 8. 向后兼容性

- 旧的 Pipeline 配置文件加载时自动迁移
- 迁移后的配置保存时会覆盖旧格式
- 不提供回退机制

---

## 9. 风险与注意事项

1. **数据丢失风险**：迁移逻辑需要充分测试
2. **UI 兼容性**：编辑旧配置时步骤的 Server 字段可能为空
3. **执行器签名变更**：需要同步修改所有调用点
