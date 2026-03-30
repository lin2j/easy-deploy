# Pipeline Step Server Configuration Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 Server 参数从 Pipeline 级别移动到 Upload 和 RemoteCommand 步骤级别，支持每个步骤独立配置 Server。

**Architecture:** 通过修改数据模型、UI 对话框和执行器，实现步骤级别的 Server 配置，同时提供数据迁移逻辑保持向后兼容。

**Tech Stack:** Java Swing, IntelliJ Platform SDK, JSON 持久化

---

## Task 1: 数据模型变更

**Files:**
- Modify: `src/main/java/io/github/yueryou/easydev/plugin/model/Pipeline.java`
- Modify: `src/main/java/io/github/yueryou/easydev/plugin/model/UploadStep.java`
- Modify: `src/main/java/io/github/yueryou/easydev/plugin/model/RemoteCommandStep.java`

### 1.1 修改 Pipeline.java - 移除 serverId 字段

- [ ] **Step 1: 打开 Pipeline.java 文件**

读取文件确认当前结构

- [ ] **Step 2: 移除 serverId 字段声明**

```java
// 删除以下行：
private String serverId;
```

- [ ] **Step 3: 移除构造函数中的 serverId 参数**

```java
// 删除以下构造函数：
public Pipeline(String name, String serverId) {
    this();
    this.name = name;
    this.serverId = serverId;
}
```

- [ ] **Step 4: 移除 getServerId 和 setServerId 方法**

```java
// 删除以下方法：
public String getServerId() {
    return serverId;
}

public void setServerId(String serverId) {
    this.serverId = serverId;
}
```

- [ ] **Step 5: 编译验证**

Run: `./gradlew compileJava`
Expected: 编译失败（因为其他文件引用了 getServerId/setServerId）

- [ ] **Step 6: 提交**

```bash
git add src/main/java/io/github/yueryou/easydev/plugin/model/Pipeline.java
git commit -m "refactor: 移除 Pipeline 类的 serverId 字段"
```

### 1.2 修改 UploadStep.java - 添加 serverId 字段

- [ ] **Step 1: 打开 UploadStep.java 文件**

- [ ] **Step 2: 添加 serverId 字段声明**

```java
public class UploadStep extends PipelineStep {
    private String uploadProfileId;
    private String serverId;  // NEW
    private boolean createRemoteDir;
    ...
}
```

- [ ] **Step 3: 添加 getter 和 setter 方法**

```java
public String getServerId() {
    return serverId;
}

public void setServerId(String serverId) {
    this.serverId = serverId;
}
```

- [ ] **Step 4: 编译验证**

Run: `./gradlew compileJava`
Expected: 编译通过

- [ ] **Step 5: 提交**

```bash
git add src/main/java/io/github/yueryou/easydev/plugin/model/UploadStep.java
git commit -m "feat: UploadStep 添加 serverId 字段"
```

### 1.3 修改 RemoteCommandStep.java - 添加 serverId 字段

- [ ] **Step 1: 打开 RemoteCommandStep.java 文件**

- [ ] **Step 2: 添加 serverId 字段声明**

```java
public class RemoteCommandStep extends PipelineStep {
    private String command;
    private String workingDir;
    private String commandId;
    private String serverId;  // NEW
    ...
}
```

- [ ] **Step 3: 添加 getter 和 setter 方法**

```java
public String getServerId() {
    return serverId;
}

public void setServerId(String serverId) {
    this.serverId = serverId;
}
```

- [ ] **Step 4: 编译验证**

Run: `./gradlew compileJava`
Expected: 编译通过

- [ ] **Step 5: 提交**

```bash
git add src/main/java/io/github/yueryou/easydev/plugin/model/RemoteCommandStep.java
git commit -m "feat: RemoteCommandStep 添加 serverId 字段"
```

---

## Task 2: 国际化资源文件更新

**Files:**
- Modify: `src/main/resources/messages_en.properties`
- Modify: `src/main/resources/messages_zh.properties`

### 2.1 添加英文资源

- [ ] **Step 1: 打开 messages_en.properties**

- [ ] **Step 2: 添加新的资源键**

```properties
# pipeline step server
pipeline.step.server=Server
pipeline.step.server.placeholder=Select a server...
pipeline.step.server.required=Server is required for this step
```

- [ ] **Step 3: 提交**

```bash
git add src/main/resources/messages_en.properties
git commit -m "i18n: 添加 Pipeline 步骤 Server 配置英文资源"
```

### 2.2 添加中文资源

- [ ] **Step 1: 打开 messages_zh.properties**

- [ ] **Step 2: 添加新的资源键**

```properties
# pipeline step server
pipeline.step.server=\u670d\u52a1\u5668
pipeline.step.server.placeholder=\u8bf7\u9009\u62e9\u4e00\u4e2a\u670d\u52a1\u5668...
pipeline.step.server.required=\u6b64\u6b65\u9aa4\u9700\u8981\u914d\u7f6e\u670d\u52a1\u5668
```

- [ ] **Step 3: 提交**

```bash
git add src/main/resources/messages_zh.properties
git commit -m "i18n: 添加 Pipeline 步骤 Server 配置中文资源"
```

---

## Task 3: UI 对话框变更

**Files:**
- Modify: `src/main/java/io/github/yueryou/easydev/plugin/ui/dialog/PipelineEditDialog.java`
- Modify: `src/main/java/io/github/yueryou/easydev/plugin/ui/dialog/StepEditDialog.java`

### 3.1 修改 PipelineEditDialog - 移除顶层 Server 选择

- [ ] **Step 1: 打开 PipelineEditDialog.java 文件**

- [ ] **Step 2: 移除 serverComboBox 字段**

```java
// 删除以下行：
private JComboBox<String> serverComboBox;
```

- [ ] **Step 3: 移除 createCenterPanel 中的 Server 选择框初始化代码**

```java
// 删除约第 66-82 行的服务器选择框初始化代码：
// 服务器选择
List<String> serverItems = new ArrayList<>();
List<SshServer> servers = ConfigHelper.sshServers();
...
serverComboBox = new JComboBox<>(serverItems.toArray(new String[0]));
...
```

- [ ] **Step 4: 移除 FormBuilder 中的 Server 组件**

```java
return FormBuilder.createFormBuilder()
        .addLabeledComponent(MessagesBundle.getText("pipeline.edit.name"), nameField)
        // 删除以下行：
        // .addLabeledComponent(MessagesBundle.getText("pipeline.edit.server"), serverComboBox)
        .addLabeledComponent(MessagesBundle.getText("pipeline.edit.failure.strategy"), failureStrategyComboBox)
        ...
```

- [ ] **Step 5: 修改 doOKAction 方法 - 移除 Server 验证逻辑**

```java
// 删除以下代码（约 134-151 行）：
// 检查是否需要 Server（上传步骤或远程命令步骤需要 Server）
boolean needServer = steps.stream().anyMatch(step ->
    step.getType() == StepType.UPLOAD || step.getType() == StepType.REMOTE_COMMAND);

String serverId = null;
if (needServer) {
    String serverSelectedItem = (String) serverComboBox.getSelectedItem();
    ...
}
```

- [ ] **Step 6: 修改 setServerId 调用**

```java
// 修改为：
pipeline.setServerId(null);  // 不再在 Pipeline 级别设置 ServerId
```

- [ ] **Step 7: 编译验证**

Run: `./gradlew compileJava`
Expected: 编译通过

- [ ] **Step 8: 提交**

```bash
git add src/main/java/io/github/yueryou/easydev/plugin/ui/dialog/PipelineEditDialog.java
git commit -m "refactor: 移除 PipelineEditDialog 顶层 Server 选择框"
```

### 3.2 修改 StepEditDialog - 添加 Server 选择框

**Files:**
- Modify: `src/main/java/io/github/yueryou/easydev/plugin/ui/dialog/StepEditDialog.java`

- [ ] **Step 1: 打开 StepEditDialog.java 文件**

- [ ] **Step 2: 添加新的字段声明**

```java
// 在类声明中添加：
private JComboBox<String> uploadServerComboBox;    // Upload 步骤的 Server 选择
private JComboBox<String> remoteServerComboBox;    // RemoteCommand 步骤的 Server 选择
```

- [ ] **Step 3: 修改 createCenterPanel 方法 - 初始化 Server 列表**

在方法开始处添加：

```java
// 获取可用 Server 列表
List<String> serverItems = new ArrayList<>();
serverItems.add("");  // 空选项
for (SshServer server : ConfigHelper.sshServers()) {
    serverItems.add(server.getId() + " - " + server.getIp() + ":" + server.getPort());
}
```

- [ ] **Step 4: 修改 createUploadPanel 方法**

```java
private JPanel createUploadPanel() {
    uploadServerComboBox = new JComboBox<>(serverItems.toArray(new String[0]));

    return FormBuilder.createFormBuilder()
            .addLabeledComponent(MessagesBundle.getText("pipeline.step.server"), uploadServerComboBox)
            .addLabeledComponent(MessagesBundle.getText("pipeline.step.upload.profile"), uploadProfileComboBox)
            .addComponent(createRemoteDirCheckBox)
            .getPanel();
}
```

- [ ] **Step 5: 修改 createRemoteCommandPanel 方法**

```java
private JPanel createRemoteCommandPanel() {
    remoteServerComboBox = new JComboBox<>(serverItems.toArray(new String[0]));

    return FormBuilder.createFormBuilder()
            .addLabeledComponent(MessagesBundle.getText("pipeline.step.server"), remoteServerComboBox)
            .addLabeledComponent(MessagesBundle.getText("pipeline.step.remote.command"), remoteCommandField)
            .addLabeledComponent(MessagesBundle.getText("pipeline.step.remote.working.dir"), remoteWorkingDirField)
            .addLabeledComponent(MessagesBundle.getText("pipeline.step.remote.use.command"), remoteCommandComboBox)
            .getPanel();
}
```

- [ ] **Step 6: 修改 populateFieldsFromStep 方法 - 填充现有步骤的 Server 值**

```java
private void populateFieldsFromStep(PipelineStep step) {
    switch (step.getType()) {
        case LOCAL_COMMAND:
            ...
            break;
        case UPLOAD:
            UploadStep uploadStep = (UploadStep) step;
            selectItemInComboBox(uploadServerComboBox, uploadStep.getServerId());
            selectItemInComboBox(uploadProfileComboBox, uploadStep.getUploadProfileId());
            createRemoteDirCheckBox.setSelected(uploadStep.isCreateRemoteDir());
            break;
        case REMOTE_COMMAND:
            RemoteCommandStep remoteStep = (RemoteCommandStep) step;
            selectItemInComboBox(remoteServerComboBox, remoteStep.getServerId());
            remoteCommandField.setText(remoteStep.getCommand());
            remoteWorkingDirField.setText(remoteStep.getWorkingDir());
            if (remoteStep.getCommandId() != null) {
                selectItemInComboBox(remoteCommandComboBox, remoteStep.getCommandId());
            }
            break;
    }
}
```

- [ ] **Step 7: 修改 createStepFromFields 方法 - 保存 Server 值到步骤对象**

```java
private PipelineStep createStepFromFields(StepType type) {
    switch (type) {
        case LOCAL_COMMAND:
            ...
            return localStep;

        case UPLOAD:
            UploadStep uploadStep = new UploadStep();
            String selectedProfile = (String) uploadProfileComboBox.getSelectedItem();
            String profileId = UiUtil.extractIdFromComboBoxItem(selectedProfile);
            if (profileId != null) {
                uploadStep.setUploadProfileId(profileId);
            }
            // 设置 ServerId
            String selectedServer = (String) uploadServerComboBox.getSelectedItem();
            String serverId = UiUtil.extractIdFromComboBoxItem(selectedServer);
            uploadStep.setServerId(serverId);

            uploadStep.setCreateRemoteDir(createRemoteDirCheckBox.isSelected());
            return uploadStep;

        case REMOTE_COMMAND:
            RemoteCommandStep remoteStep = new RemoteCommandStep();
            remoteStep.setCommand(remoteCommandField.getText());
            remoteStep.setWorkingDir(remoteWorkingDirField.getText());

            // 设置 ServerId
            String selectedRemoteServer = (String) remoteServerComboBox.getSelectedItem();
            String remoteServerId = UiUtil.extractIdFromComboBoxItem(selectedRemoteServer);
            remoteStep.setServerId(remoteServerId);

            // 检查是否选择了已有命令
            String selectedRemoteCommand = (String) remoteCommandComboBox.getSelectedItem();
            String remoteCommandId = UiUtil.extractIdFromComboBoxItem(selectedRemoteCommand);
            if (remoteCommandId != null) {
                remoteStep.setCommandId(remoteCommandId);
            }

            return remoteStep;

        default:
            throw new IllegalArgumentException("Unknown step type: " + type);
    }
}
```

- [ ] **Step 8: 添加必要的 import**

```java
import tech.lin2j.idea.plugin.model.SshServer;
import tech.lin2j.idea.plugin.model.ConfigHelper;
```

- [ ] **Step 9: 编译验证**

Run: `./gradlew compileJava`
Expected: 编译通过

- [ ] **Step 10: 提交**

```bash
git add src/main/java/io/github/yueryou/easydev/plugin/ui/dialog/StepEditDialog.java
git commit -m "feat: StepEditDialog 添加 Server 选择框"
```

---

## Task 4: 执行器变更

**Files:**
- Modify: `src/main/java/io/github/yueryou/easydev/plugin/executor/PipelineExecutor.java`
- Modify: `src/main/java/io/github/yueryou/easydev/plugin/executor/UploadExecutor.java`
- Modify: `src/main/java/io/github/yueryou/easydev/plugin/executor/RemoteCommandExecutor.java`

### 4.1 读取现有执行器代码

- [ ] **Step 1: 读取 UploadExecutor.java**

确认当前方法签名

- [ ] **Step 2: 读取 RemoteCommandExecutor.java**

确认当前方法签名

### 4.2 修改 PipelineExecutor.java

- [ ] **Step 1: 打开 PipelineExecutor.java 文件**

- [ ] **Step 2: 修改 executeStep 私有方法**

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
                return UploadExecutor.execute(uploadStep, context, uploadServer);
            } else {
                context.getLogConsumer().accept("[错误] Upload 步骤未配置 Server: " + step.getName());
                return StepResult.failure("Upload 步骤未配置 Server");
            }
        case REMOTE_COMMAND:
            RemoteCommandStep remoteStep = (RemoteCommandStep) step;
            if (remoteStep.getServerId() != null) {
                SshServer remoteServer = ConfigHelper.getSshServerById(
                    Integer.parseInt(remoteStep.getServerId())
                );
                return RemoteCommandExecutor.execute(remoteStep, context, remoteServer);
            } else {
                context.getLogConsumer().accept("[错误] RemoteCommand 步骤未配置 Server: " + step.getName());
                return StepResult.failure("RemoteCommand 步骤未配置 Server");
            }
        default:
            return StepResult.failure("不支持的步骤类型：" + step.getType());
    }
}
```

- [ ] **Step 3: 修改 executeFromStep 方法 - 移除 Server 日志**

```java
// 删除或修改以下行：
logConsumer.accept("服务器：" + server.getIp() + ":" + server.getPort());
// 改为在步骤执行时输出各步骤的 Server
```

- [ ] **Step 4: 添加必要的 import**

```java
import io.github.yueryou.easydev.plugin.model.UploadStep;
import io.github.yueryou.easydev.plugin.model.RemoteCommandStep;
```

- [ ] **Step 5: 编译验证**

Run: `./gradlew compileJava`
Expected: 可能失败（等待 UploadExecutor 和 RemoteCommandExecutor 修改）

- [ ] **Step 6: 提交**

```bash
git add src/main/java/io/github/yueryou/easydev/plugin/executor/PipelineExecutor.java
git commit -m "refactor: PipelineExecutor 从步骤获取 Server 配置"
```

### 4.3 修改 UploadExecutor.java

- [ ] **Step 1: 读取 UploadExecutor.java 当前内容**

- [ ] **Step 2: 修改 execute 方法签名**

```java
// 原签名可能是：
public static StepResult execute(UploadStep step, ExecutionContext context)

// 修改为：
public static StepResult execute(UploadStep step, ExecutionContext context, SshServer server)
```

- [ ] **Step 3: 检查并修改所有使用 context.getServer() 的地方**

由于现在 Server 是从步骤参数传入，需要将原来使用 `context.getServer()` 的地方改为使用传入的 `server` 参数。

- [ ] **Step 4: 编译验证**

Run: `./gradlew compileJava`
Expected: 编译通过

- [ ] **Step 5: 提交**

```bash
git add src/main/java/io/github/yueryou/easydev/plugin/executor/UploadExecutor.java
git commit -m "refactor: UploadExecutor 接受 SshServer 参数"
```

### 4.4 修改 RemoteCommandExecutor.java

- [ ] **Step 1: 读取 RemoteCommandExecutor.java 当前内容**

- [ ] **Step 2: 修改 execute 方法签名**

```java
// 原签名可能是：
public static StepResult execute(RemoteCommandStep step, ExecutionContext context)

// 修改为：
public static StepResult execute(RemoteCommandStep step, ExecutionContext context, SshServer server)
```

- [ ] **Step 3: 检查并修改所有使用 context.getServer() 的地方**

- [ ] **Step 4: 编译验证**

Run: `./gradlew compileJava`
Expected: 编译通过

- [ ] **Step 5: 提交**

```bash
git add src/main/java/io/github/yueryou/easydev/plugin/executor/RemoteCommandExecutor.java
git commit -m "refactor: RemoteCommandExecutor 接受 SshServer 参数"
```

---

## Task 5: 数据迁移逻辑

**Files:**
- Modify: `src/main/java/io/github/yueryou/easydev/plugin/model/PipelineConfigPersistence.java`

### 5.1 添加数据迁移逻辑

- [ ] **Step 1: 打开 PipelineConfigPersistence.java 文件**

- [ ] **Step 2: 读取 getAllPipelines 方法**

- [ ] **Step 3: 修改 getAllPipelines 方法 - 添加迁移逻辑**

```java
public static List<Pipeline> getAllPipelines() {
    List<Pipeline> pipelines = loadPipelines();  // 实际加载方法名可能不同
    for (Pipeline pipeline : pipelines) {
        migratePipeline(pipeline);
    }
    return pipelines;
}
```

- [ ] **Step 4: 添加 migratePipeline 私有方法**

```java
private static void migratePipeline(Pipeline pipeline) {
    // 向后兼容：如果 Pipeline 有 serverId，迁移到所有 UPLOAD/REMOTE_COMMAND 步骤
    // 注意：由于 Pipeline 类已经移除了 serverId 字段，这里需要检查 JSON 中是否还有该字段
    // 如果 JSON 反序列化时 serverId 被忽略，则此迁移逻辑可能不需要

    // 如果 Pipeline 类保留了临时的 serverId 字段用于迁移，可以使用以下逻辑：
    // if (pipeline.getServerId() != null) {
    //     for (PipelineStep step : pipeline.getSteps()) {
    //         if (step instanceof UploadStep) {
    //             ((UploadStep) step).setServerId(pipeline.getServerId());
    //         } else if (step instanceof RemoteCommandStep) {
    //             ((RemoteCommandStep) step).setServerId(pipeline.getServerId());
    //         }
    //     }
    // }
}
```

**注意：** 由于 Pipeline 类的 `serverId` 字段已被移除，JSON 反序列化时该字段可能会被忽略。需要确认 JSON 库的行为。

- [ ] **Step 5: 编译验证**

Run: `./gradlew compileJava`
Expected: 编译通过

- [ ] **Step 6: 提交**

```bash
git add src/main/java/io/github/yueryou/easydev/plugin/model/PipelineConfigPersistence.java
git commit -m "feat: 添加 Pipeline ServerId 迁移逻辑"
```

---

## Task 6: 修改 CommandPipelinePanel

**Files:**
- Modify: `src/main/java/io/github/yueryou/easydev/plugin/ui/component/CommandPipelinePanel.java`

### 6.1 移除 Server 验证逻辑

- [ ] **Step 1: 打开 CommandPipelinePanel.java 文件**

- [ ] **Step 2: 读取 executePipelineFromStep 方法**

- [ ] **Step 3: 移除 Server 验证代码**

```java
// 删除或修改以下代码（约 172-180 行）：
// 检查是否需要 Server（上传步骤或远程命令步骤需要 Server）
boolean needServer = pipeline.getSteps().stream().anyMatch(step ->
    step.getType() == StepType.UPLOAD || step.getType() == StepType.REMOTE_COMMAND);

if (needServer && pipeline.getServerId() == null) {
    notificationService.showNotification(project, MessagesBundle.getText("pipeline.notification.title.running"),
        MessagesBundle.getText("pipeline.error.no.server"));
    return;
}
```

由于 Server 现在在步骤级别，验证逻辑移至执行器中。

- [ ] **Step 4: 修改 executePipelineFromStep 方法 - 移除传递给执行器的 server 参数获取**

```java
// 原来的代码可能像这样：
SshServer server = ConfigHelper.getSshServerById(Integer.parseInt(pipeline.getServerId()));
PipelineResult result = PipelineExecutor.executeFromStep(pipeline, server, project, logConsumer, startIndex);

// 需要修改为（executor 内部会处理）：
PipelineResult result = PipelineExecutor.executeFromStep(pipeline, null, project, logConsumer, startIndex);
// 或者修改 executeFromStep 签名，移除 server 参数
```

- [ ] **Step 5: 移除不需要的 import**

```java
// 可能可以移除：
import io.github.yueryou.easydev.plugin.model.StepType;
```

- [ ] **Step 6: 编译验证**

Run: `./gradlew compileJava`
Expected: 编译通过

- [ ] **Step 7: 提交**

```bash
git add src/main/java/io/github/yueryou/easydev/plugin/ui/component/CommandPipelinePanel.java
git commit -m "refactor: CommandPipelinePanel 移除 Server 验证逻辑"
```

---

## Task 7: 完整测试

**Files:**
- 所有上述修改的文件

### 7.1 编译验证

- [ ] **Step 1: 清理构建**

Run: `./gradlew clean`
Expected: 成功

- [ ] **Step 2: 编译全部代码**

Run: `./gradlew compileJava`
Expected: 编译通过，无错误

- [ ] **Step 3: 运行测试（如果有）**

Run: `./gradlew test`
Expected: 所有测试通过

### 7.2 运行 IDE 测试

- [ ] **Step 4: 启动 IDE 进行测试**

Run: `./gradlew runIde`
Expected: IDE 正常启动

### 7.3 手动测试场景

- [ ] **Step 5: 测试场景 1 - 纯本地流水线**

在 IDE 中：
1. 创建新 Pipeline
2. 添加 LOCAL_COMMAND 步骤
3. 不选择 Server（步骤编辑对话框不应显示 Server 选择）
4. 保存并运行
Expected: 正常执行

- [ ] **Step 6: 测试场景 2 - Upload 步骤配置 Server**

在 IDE 中：
1. 创建新 Pipeline
2. 添加 UPLOAD 步骤
3. 选择 Server
4. 选择 Upload Profile
5. 保存并运行
Expected: 正常执行，日志显示使用了正确的 Server

- [ ] **Step 7: 测试场景 3 - RemoteCommand 步骤配置 Server**

在 IDE 中：
1. 创建新 Pipeline
2. 添加 REMOTE_COMMAND 步骤
3. 选择 Server
4. 输入命令
5. 保存并运行
Expected: 正常执行，日志显示使用了正确的 Server

- [ ] **Step 8: 测试场景 4 - 多服务器流水线**

在 IDE 中：
1. 创建新 Pipeline
2. 添加多个步骤，每个步骤选择不同 Server
3. 保存并运行
Expected: 每个步骤使用对应的 Server 执行

- [ ] **Step 9: 测试场景 5 - 数据迁移**

1. 使用旧版本插件创建包含 Server 的 Pipeline
2. 使用新版本插件加载该 Pipeline
3. 编辑 Pipeline，检查步骤的 Server 是否正确迁移
Expected: Server 配置自动迁移到步骤级别

### 7.4 最终提交

- [ ] **Step 10: 所有测试通过后提交最终变更**

```bash
git add .
git commit -m "feat: 支持 Pipeline 步骤级别独立配置 Server"
```

---

## 回滚计划

如果实施过程中遇到问题，按以下顺序回滚：

1. **回滚 UI 变更**: 恢复 PipelineEditDialog 和 StepEditDialog
2. **回滚执行器变更**: 恢复 PipelineExecutor、UploadExecutor、RemoteCommandExecutor
3. **回滚数据模型**: 恢复 Pipeline 的 serverId 字段

---

## 注意事项

1. **JSON 兼容性**: 移除 Pipeline 的 serverId 字段后，旧的 JSON 配置文件加载时该字段会被忽略。需要确认是否需要显式的迁移逻辑。

2. **空指针处理**: 所有访问 `serverId` 的地方都需要检查 null。

3. **向后兼容**: 确保旧 Pipeline 配置能够正常加载，Server 配置迁移到步骤级别。

4. **UI 体验**: 编辑旧 Pipeline 时，步骤的 Server 字段可能为空，需要提示用户重新选择。
