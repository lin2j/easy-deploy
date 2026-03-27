# CommandPipelinePanel 用户自定义流水线功能实现计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 CommandPipelinePanel 中实现用户自定义流水线功能，支持创建、编辑、删除流水线，执行包含本地命令、上传文件、远程命令的自动化部署流程。

**Architecture:** 采用分层架构：数据模型层（model）→ 持久化层（persistence）→ 执行引擎（executor）→ UI 层（ui/component 和 ui/dialog）。流水线配置独立存储在 `.idea/easy-deploy/pipelines.json` 文件中。

**Tech Stack:** Java 11+, IntelliJ Platform SDK, sshj library, Swing UI components (JBLabel, JBList, JBTextField, FormBuilder, ToolbarDecorator, DialogWrapper).

---

## Chunk 1: 数据模型和持久化层

### Task 1: 创建基础数据模型类

**Files:**
- Create: `src/main/java/io/github/yueryou/easydev/plugin/model/Pipeline.java`
- Create: `src/main/java/io/github/yueryou/easydev/plugin/model/PipelineStep.java`
- Create: `src/main/java/io/github/yueryou/easydev/plugin/model/StepType.java`
- Create: `src/main/java/io/github/yueryou/easydev/plugin/model/FailureStrategy.java`
- Create: `src/main/java/io/github/yueryou/easydev/plugin/model/LocalCommandStep.java`
- Create: `src/main/java/io/github/yueryou/easydev/plugin/model/UploadStep.java`
- Create: `src/main/java/io/github/yueryou/easydev/plugin/model/RemoteCommandStep.java`

- [ ] **Step 1.1: 创建 StepType 枚举**

创建 `src/main/java/io/github/yueryou/easydev/plugin/model/StepType.java`:

```java
package io.github.yueryou.easydev.plugin.model;

/**
 * 流水线步骤类型
 */
public enum StepType {
    /**
     * 本地命令步骤
     */
    LOCAL_COMMAND,

    /**
     * 上传文件步骤
     */
    UPLOAD,

    /**
     * 远程命令步骤
     */
    REMOTE_COMMAND
}
```

- [ ] **Step 1.2: 创建 FailureStrategy 枚举**

创建 `src/main/java/io/github/yueryou/easydev/plugin/model/FailureStrategy.java`:

```java
package io.github.yueryou.easydev.plugin.model;

/**
 * 流水线失败处理策略
 */
public enum FailureStrategy {
    /**
     * 失败即停止
     */
    STOP,

    /**
     * 继续执行
     */
    CONTINUE
}
```

- [ ] **Step 1.3: 创建 PipelineStep 抽象基类**

创建 `src/main/java/io/github/yueryou/easydev/plugin/model/PipelineStep.java`:

```java
package io.github.yueryou.easydev.plugin.model;

import tech.lin2j.idea.plugin.model.UniqueModel;

/**
 * 流水线步骤基类
 */
public abstract class PipelineStep implements UniqueModel {

    protected String uid;

    /**
     * 步骤名称，用于显示
     */
    protected String name;

    /**
     * 步骤类型
     */
    protected StepType type;

    /**
     * 是否启用
     */
    protected Boolean enabled = true;

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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
```

- [ ] **Step 1.4: 创建 LocalCommandStep 类**

创建 `src/main/java/io/github/yueryou/easydev/plugin/model/LocalCommandStep.java`:

```java
package io.github.yueryou.easydev.plugin.model;

/**
 * 本地命令步骤
 */
public class LocalCommandStep extends PipelineStep {

    /**
     * 命令内容
     */
    private String command;

    /**
     * 工作目录
     */
    private String workingDir;

    /**
     * 超时时间（秒），0 表示不限制
     */
    private Integer timeout;

    /**
     * 关联的 Command ID（可选，用于引用已有命令）
     */
    private Integer commandId;

    public LocalCommandStep() {
        this.type = StepType.LOCAL_COMMAND;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getCommandId() {
        return commandId;
    }

    public void setCommandId(Integer commandId) {
        this.commandId = commandId;
    }
}
```

- [ ] **Step 1.5: 创建 UploadStep 类**

创建 `src/main/java/io/github/yueryou/easydev/plugin/model/UploadStep.java`:

```java
package io.github.yueryou.easydev.plugin.model;

/**
 * 上传文件步骤
 */
public class UploadStep extends PipelineStep {

    /**
     * 关联的 UploadProfile ID
     */
    private Integer uploadProfileId;

    /**
     * 是否创建远程目录
     */
    private Boolean createRemoteDir = true;

    public UploadStep() {
        this.type = StepType.UPLOAD;
    }

    public Integer getUploadProfileId() {
        return uploadProfileId;
    }

    public void setUploadProfileId(Integer uploadProfileId) {
        this.uploadProfileId = uploadProfileId;
    }

    public Boolean getCreateRemoteDir() {
        return createRemoteDir;
    }

    public void setCreateRemoteDir(Boolean createRemoteDir) {
        this.createRemoteDir = createRemoteDir;
    }
}
```

- [ ] **Step 1.6: 创建 RemoteCommandStep 类**

创建 `src/main/java/io/github/yueryou/easydev/plugin/model/RemoteCommandStep.java`:

```java
package io.github.yueryou.easydev.plugin.model;

/**
 * 远程命令步骤
 */
public class RemoteCommandStep extends PipelineStep {

    /**
     * 命令内容
     */
    private String command;

    /**
     * 远程工作目录
     */
    private String workingDir;

    /**
     * 关联的 Command ID（可选，用于引用已有命令）
     */
    private Integer commandId;

    public RemoteCommandStep() {
        this.type = StepType.REMOTE_COMMAND;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public Integer getCommandId() {
        return commandId;
    }

    public void setCommandId(Integer commandId) {
        this.commandId = commandId;
    }
}
```

- [ ] **Step 1.7: 创建 Pipeline 类**

创建 `src/main/java/io/github/yueryou/easydev/plugin/model/Pipeline.java`:

```java
package io.github.yueryou.easydev.plugin.model;

import tech.lin2j.idea.plugin.model.UniqueModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 流水线配置
 */
public class Pipeline implements UniqueModel {

    private Integer id;

    private String uid;

    /**
     * 流水线名称
     */
    private String name;

    /**
     * 目标 SSH 服务器 ID
     */
    private Integer serverId;

    /**
     * 步骤列表
     */
    private List<PipelineStep> steps = new ArrayList<>();

    /**
     * 失败策略
     */
    private FailureStrategy onFailure = FailureStrategy.STOP;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    public Pipeline() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public void setUid(String uid) {
        this.uid = uid;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    public List<PipelineStep> getSteps() {
        return steps;
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pipeline pipeline = (Pipeline) o;
        return Objects.equals(id, pipeline.id) || Objects.equals(uid, pipeline.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uid);
    }
}
```

- [ ] **Step 1.8: 验证编译**

运行命令：
```bash
./gradlew compileJava
```
预期：编译成功，无错误

- [ ] **Step 1.9: 提交**

```bash
git add src/main/java/io/github/yueryou/easydev/plugin/model/*.java
git commit -m "feat(pipeline): add data models for pipeline feature

- Add Pipeline, PipelineStep, StepType, FailureStrategy classes
- Add LocalCommandStep, UploadStep, RemoteCommandStep step types
- All step types implement UniqueModel interface
- Pipeline supports failure strategy configuration"
```

---

### Task 2: 创建执行上下文和步骤结果类

**Files:**
- Create: `src/main/java/io/github/yueryou/easydev/plugin/model/StepResult.java`
- Create: `src/main/java/io/github/yueryou/easydev/plugin/model/ExecutionContext.java`
- Create: `src/main/java/io/github/yueryou/easydev/plugin/util/VariableResolver.java`

- [ ] **Step 2.1: 创建 StepResult 类**

创建 `src/main/java/io/github/yueryou/easydev/plugin/model/StepResult.java`:

```java
package io.github.yueryou.easydev.plugin.model;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 步骤执行结果
 */
public class StepResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 退出码
     */
    private int exitCode;

    /**
     * 标准输出
     */
    private String stdout;

    /**
     * 错误输出
     */
    private String stderr;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 执行耗时
     */
    private Duration duration;

    /**
     * 步骤特定的输出（用于变量传递）
     */
    private Map<String, Object> outputs = new HashMap<>();

    public static StepResult success(String stdout, int exitCode) {
        StepResult result = new StepResult();
        result.success = true;
        result.exitCode = exitCode;
        result.stdout = stdout;
        return result;
    }

    public static StepResult failure(String errorMessage) {
        StepResult result = new StepResult();
        result.success = false;
        result.errorMessage = errorMessage;
        return result;
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
}
```

- [ ] **Step 2.2: 创建 VariableResolver 工具类**

创建 `src/main/java/io/github/yueryou/easydev/plugin/util/VariableResolver.java`:

```java
package io.github.yueryou.easydev.plugin.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 变量解析器 - 解析 {{var.name}} 格式的模板变量
 */
public class VariableResolver {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    /**
     * 解析模板字符串中的变量
     *
     * @param template 模板字符串，如 "Hello {{name}}"
     * @param variables 变量映射表
     * @return 解析后的字符串
     */
    public static String resolve(String template, Map<String, Object> variables) {
        if (template == null || template.isEmpty()) {
            return template;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = variables.get(varName);
            String replacement = value != null ? value.toString() : matcher.group(0);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 检查模板是否包含指定变量
     */
    public static boolean containsVariable(String template, String varName) {
        if (template == null || template.isEmpty()) {
            return false;
        }
        return template.contains("{{" + varName + "}}");
    }
}
```

- [ ] **Step 2.3: 创建 ExecutionContext 类**

创建 `src/main/java/io/github/yueryou/easydev/plugin/model/ExecutionContext.java`:

```java
package io.github.yueryou.easydev.plugin.model;

import com.intellij.openapi.project.Project;
import tech.lin2j.idea.plugin.ssh.SshServer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 流水线执行上下文
 */
public class ExecutionContext {

    private final Pipeline pipeline;
    private final SshServer server;
    private final Project project;
    private final Consumer<String> logConsumer;
    private final Map<String, Object> variables;

    /**
     * 起始步骤索引（用于从指定步骤开始执行）
     */
    private int startIndex = 0;

    public ExecutionContext(Pipeline pipeline, SshServer server, Project project, Consumer<String> logConsumer) {
        this.pipeline = pipeline;
        this.server = server;
        this.project = project;
        this.logConsumer = logConsumer;
        this.variables = new HashMap<>();

        // 初始化预设变量
        initPredefinedVariables();
    }

    private void initPredefinedVariables() {
        // 时间戳
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        variables.put("timestamp", timestamp);

        // 项目根目录
        if (project != null && project.getBasePath() != null) {
            variables.put("workspace", project.getBasePath());
        }

        // 用户主目录
        variables.put("user.home", System.getProperty("user.home"));

        // SSH 服务器信息
        if (server != null) {
            variables.put("server.host", server.getHost());
            variables.put("server.port", String.valueOf(server.getPort()));
            variables.put("server.username", server.getUsername());
        }
    }

    /**
     * 解析模板字符串
     */
    public String resolve(String template) {
        return io.github.yueryou.easydev.plugin.util.VariableResolver.resolve(template, variables);
    }

    /**
     * 添加步骤输出到上下文
     */
    public void addStepOutput(String stepUid, StepResult result) {
        variables.put(stepUid + ".stdout", result.getStdout());
        variables.put(stepUid + ".exitCode", result.getExitCode());

        // 添加步骤特定输出
        if (result.getOutputs() != null) {
            for (Map.Entry<String, Object> entry : result.getOutputs().entrySet()) {
                variables.put(stepUid + "." + entry.getKey(), entry.getValue());
            }
        }
    }

    // Getters

    public Pipeline getPipeline() {
        return pipeline;
    }

    public SshServer getServer() {
        return server;
    }

    public Project getProject() {
        return project;
    }

    public Consumer<String> getLogConsumer() {
        return logConsumer;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }
}
```

- [ ] **Step 2.4: 验证编译**

运行命令：
```bash
./gradlew compileJava
```
预期：编译成功，无错误

- [ ] **Step 2.5: 提交**

```bash
git add src/main/java/io/github/yueryou/easydev/plugin/model/StepResult.java \
        src/main/java/io/github/yueryou/easydev/plugin/model/ExecutionContext.java \
        src/main/java/io/github/yueryou/easydev/plugin/util/VariableResolver.java
git commit -m "feat(pipeline): add execution context and variable resolver

- Add StepResult class for step execution results
- Add ExecutionContext with predefined variables (timestamp, workspace, etc.)
- Add VariableResolver for {{var.name}} template syntax
- Support step output variables for data passing between steps"
```

---

### Task 3: 创建流水线执行器

**Files:**
- Create: `src/main/java/io/github/yueryou/easydev/plugin/executor/PipelineExecutor.java`
- Create: `src/main/java/io/github/yueryou/easydev/plugin/executor/LocalCommandExecutor.java`
- Create: `src/main/java/io/github/yueryou/easydev/plugin/executor/UploadExecutor.java`
- Create: `src/main/java/io/github/yueryou/easydev/plugin/executor/RemoteCommandExecutor.java`

- [ ] **Step 3.1: 创建 LocalCommandExecutor 类**

创建 `src/main/java/io/github/yueryou/easydev/plugin/executor/LocalCommandExecutor.java`:

```java
package io.github.yueryou.easydev.plugin.executor;

import io.github.yueryou.easydev.plugin.model.ExecutionContext;
import io.github.yueryou.easydev.plugin.model.LocalCommandStep;
import io.github.yueryou.easydev.plugin.model.PipelineStep;
import io.github.yueryou.easydev.plugin.model.StepResult;
import tech.lin2j.idea.plugin.model.Command;
import tech.lin2j.idea.plugin.model.ConfigHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 本地命令执行器
 */
public class LocalCommandExecutor {

    /**
     * 执行本地命令步骤
     */
    public static StepResult execute(PipelineStep step, ExecutionContext context) {
        if (!(step instanceof LocalCommandStep)) {
            return StepResult.failure("Invalid step type: expected LocalCommandStep");
        }

        LocalCommandStep localStep = (LocalCommandStep) step;
        String logPrefix = "[LocalCommand] ";

        try {
            // 解析变量
            String command = context.resolve(localStep.getCommand());
            String workingDir = context.resolve(localStep.getWorkingDir());

            context.getLogConsumer().accept(logPrefix + "Executing: " + command);

            // 如果配置了 commandId，使用已有命令
            if (localStep.getCommandId() != null) {
                Command cmd = ConfigHelper.getCommandById(localStep.getCommandId());
                if (cmd != null) {
                    command = cmd.generateCmdLine(workingDir);
                    context.getLogConsumer().accept(logPrefix + "Using existing command: " + cmd.getTitle());
                }
            } else if (workingDir != null && !workingDir.isEmpty()) {
                command = "cd " + workingDir + " && " + command;
            }

            // 执行命令
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            if (workingDir != null && !workingDir.isEmpty()) {
                processBuilder.directory(new java.io.File(workingDir));
            }
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // 读取输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // 等待完成（支持超时）
            Integer timeout = localStep.getTimeout();
            boolean completed;
            if (timeout != null && timeout > 0) {
                completed = process.waitFor(timeout, TimeUnit.SECONDS);
            } else {
                completed = process.waitFor();
            }

            if (!completed) {
                process.destroyForcibly();
                return StepResult.failure(logPrefix + "Command timed out after " + timeout + " seconds");
            }

            int exitCode = process.exitValue();
            String stdout = output.toString();

            if (exitCode == 0) {
                context.getLogConsumer().accept(logPrefix + "Command completed successfully");
                StepResult result = StepResult.success(stdout, exitCode);
                context.addStepOutput(step.getUid(), result);
                return result;
            } else {
                context.getLogConsumer().accept(logPrefix + "Command failed with exit code: " + exitCode);
                StepResult result = StepResult.failure(logPrefix + "Exit code: " + exitCode);
                result.setExitCode(exitCode);
                result.setStdout(stdout);
                context.addStepOutput(step.getUid(), result);
                return result;
            }

        } catch (IOException e) {
            context.getLogConsumer().accept(logPrefix + "IO Error: " + e.getMessage());
            return StepResult.failure(logPrefix + "IO Error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            context.getLogConsumer().accept(logPrefix + "Interrupted");
            return StepResult.failure(logPrefix + "Interrupted");
        }
    }
}
```

- [ ] **Step 3.2: 创建 UploadExecutor 类**

创建 `src/main/java/io/github/yueryou/easydev/plugin/executor/UploadExecutor.java`:

```java
package io.github.yueryou.easydev.plugin.executor;

import io.github.yueryou.easydev.plugin.model.ExecutionContext;
import io.github.yueryou.easydev.plugin.model.PipelineStep;
import io.github.yueryou.easydev.plugin.model.StepResult;
import io.github.yueryou.easydev.plugin.model.UploadStep;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.UploadProfile;
import tech.lin2j.idea.plugin.service.ISshService;
import tech.lin2j.idea.plugin.service.impl.SshjSshService;

import java.io.File;

/**
 * 上传文件执行器
 */
public class UploadExecutor {

    /**
     * 执行上传步骤
     */
    public static StepResult execute(PipelineStep step, ExecutionContext context) {
        if (!(step instanceof UploadStep)) {
            return StepResult.failure("Invalid step type: expected UploadStep");
        }

        UploadStep uploadStep = (UploadStep) step;
        String logPrefix = "[Upload] ";

        try {
            // 获取 UploadProfile
            Integer profileId = uploadStep.getUploadProfileId();
            if (profileId == null) {
                return StepResult.failure(logPrefix + "Upload profile ID is null");
            }

            UploadProfile profile = ConfigHelper.getUploadProfileById(profileId);
            if (profile == null) {
                return StepResult.failure(logPrefix + "Upload profile not found: " + profileId);
            }

            context.getLogConsumer().accept(logPrefix + "Uploading using profile: " + profile.getName());

            // 解析本地文件路径（支持变量）
            String localFile = context.resolve(profile.getFile());
            String location = context.resolve(profile.getLocation());

            // 验证本地文件存在
            File file = new File(localFile);
            if (!file.exists()) {
                return StepResult.failure(logPrefix + "Local file not found: " + localFile);
            }

            // 执行上传
            ISshService sshService = new SshjSshService();
            boolean createRemoteDir = uploadStep.getCreateRemoteDir() != null ?
                    uploadStep.getCreateRemoteDir() : true;

            // 使用 SshjConnection 进行上传（需要适配现有 API）
            // 注意：这里需要适配现有的上传 API
            boolean success = sshService.upload(
                    null, // filter - 暂不使用
                    context.getServer(),
                    localFile,
                    location,
                    null // listener - 暂不使用
            ).isSuccess(); // 假设返回 SshStatus 类型

            if (success) {
                context.getLogConsumer().accept(logPrefix + "Upload completed: " + localFile + " -> " + location);
                StepResult result = StepResult.success("Uploaded: " + localFile, 0);
                result.addOutput("uploadedFiles", localFile);
                result.addOutput("remotePath", location);
                context.addStepOutput(step.getUid(), result);
                return result;
            } else {
                context.getLogConsumer().accept(logPrefix + "Upload failed");
                return StepResult.failure(logPrefix + "Upload failed");
            }

        } catch (Exception e) {
            context.getLogConsumer().accept(logPrefix + "Error: " + e.getMessage());
            return StepResult.failure(logPrefix + "Error: " + e.getMessage());
        }
    }
}
```

- [ ] **Step 3.3: 创建 RemoteCommandExecutor 类**

创建 `src/main/java/io/github/yueryou/easydev/plugin/executor/RemoteCommandExecutor.java`:

```java
package io.github.yueryou.easydev.plugin.executor;

import io.github.yueryou.easydev.plugin.model.ExecutionContext;
import io.github.yueryou.easydev.plugin.model.PipelineStep;
import io.github.yueryou.easydev.plugin.model.RemoteCommandStep;
import io.github.yueryou.easydev.plugin.model.StepResult;
import tech.lin2j.idea.plugin.model.Command;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.service.ISshService;
import tech.lin2j.idea.plugin.service.impl.SshjSshService;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.SshStatus;

/**
 * 远程命令执行器
 */
public class RemoteCommandExecutor {

    /**
     * 执行远程命令步骤
     */
    public static StepResult execute(PipelineStep step, ExecutionContext context) {
        if (!(step instanceof RemoteCommandStep)) {
            return StepResult.failure("Invalid step type: expected RemoteCommandStep");
        }

        RemoteCommandStep remoteStep = (RemoteCommandStep) step;
        String logPrefix = "[RemoteCommand] ";

        try {
            // 解析变量
            String command = context.resolve(remoteStep.getCommand());

            // 如果配置了 commandId，使用已有命令
            if (remoteStep.getCommandId() != null) {
                Command cmd = ConfigHelper.getCommandById(remoteStep.getCommandId());
                if (cmd != null) {
                    command = cmd.generateCmdLine(remoteStep.getWorkingDir());
                    context.getLogConsumer().accept(logPrefix + "Using existing command: " + cmd.getTitle());
                }
            }

            context.getLogConsumer().accept(logPrefix + "Executing on remote: " + command);

            // 执行远程命令
            ISshService sshService = new SshjSshService();
            SshServer server = context.getServer();

            if (server == null) {
                return StepResult.failure(logPrefix + "SSH server is null");
            }

            SshStatus status = sshService.execute(server, command);

            if (status.isSuccess()) {
                context.getLogConsumer().accept(logPrefix + "Remote command completed successfully");
                StepResult result = StepResult.success(status.getOutput(), 0);
                context.addStepOutput(step.getUid(), result);
                return result;
            } else {
                context.getLogConsumer().accept(logPrefix + "Remote command failed: " + status.getMessage());
                return StepResult.failure(logPrefix + status.getMessage());
            }

        } catch (Exception e) {
            context.getLogConsumer().accept(logPrefix + "Error: " + e.getMessage());
            return StepResult.failure(logPrefix + "Error: " + e.getMessage());
        }
    }
}
```

- [ ] **Step 3.4: 创建 PipelineExecutor 主执行器**

创建 `src/main/java/io/github/yueryou/easydev/plugin/executor/PipelineExecutor.java`:

```java
package io.github.yueryou.easydev.plugin.executor;

import com.intellij.openapi.project.Project;
import io.github.yueryou.easydev.plugin.model.*;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.ssh.SshServer;

import java.util.List;
import java.util.function.Consumer;

/**
 * 流水线主执行器
 */
public class PipelineExecutor {

    /**
     * 执行流水线（从头开始）
     */
    public static PipelineResult execute(Pipeline pipeline, SshServer server, Project project, Consumer<String> logConsumer) {
        return executeFromStep(pipeline, server, project, logConsumer, 0);
    }

    /**
     * 从指定步骤开始执行流水线
     *
     * @param pipeline 流水线配置
     * @param server SSH 服务器
     * @param project 项目
     * @param logConsumer 日志消费者
     * @param startIndex 起始步骤索引
     * @return 执行结果
     */
    public static PipelineResult executeFromStep(
            Pipeline pipeline,
            SshServer server,
            Project project,
            Consumer<String> logConsumer,
            int startIndex) {

        logConsumer.accept("========================================");
        logConsumer.accept("Starting pipeline: " + pipeline.getName());
        logConsumer.accept("Target server: " + server.getHost() + ":" + server.getPort());
        if (startIndex > 0) {
            logConsumer.accept("Starting from step index: " + startIndex);
        }
        logConsumer.accept("========================================");

        ExecutionContext context = new ExecutionContext(pipeline, server, project, logConsumer);
        context.setStartIndex(startIndex);

        List<PipelineStep> steps = pipeline.getSteps();
        PipelineResult result = new PipelineResult();

        for (int i = 0; i < steps.size(); i++) {
            PipelineStep step = steps.get(i);

            // 跳过起始步骤之前的步骤
            if (i < startIndex) {
                logConsumer.accept("[Step " + (i + 1) + "] Skipped: " + step.getName());
                result.addSkippedStep(step);
                continue;
            }

            // 检查步骤是否启用
            if (Boolean.FALSE.equals(step.getEnabled())) {
                logConsumer.accept("[Step " + (i + 1) + "] Disabled, skipping: " + step.getName());
                result.addSkippedStep(step);
                continue;
            }

            logConsumer.accept("----------------------------------------");
            logConsumer.accept("[Step " + (i + 1) + "/" + steps.size() + "] Starting: " + step.getName() + " (" + step.getType() + ")");

            StepResult stepResult = executeStep(step, context);
            result.addStepResult(step, stepResult);

            if (stepResult.isSuccess()) {
                logConsumer.accept("[Step " + (i + 1) + "] Completed: " + step.getName());
            } else {
                logConsumer.accept("[Step " + (i + 1) + "] Failed: " + step.getName());

                // 检查失败策略
                if (pipeline.getOnFailure() == FailureStrategy.STOP) {
                    logConsumer.accept("Failure strategy is STOP, aborting pipeline");
                    result.setSuccess(false);
                    result.setFailedStep(step);
                    return result;
                } else {
                    logConsumer.accept("Failure strategy is CONTINUE, continuing to next step");
                }
            }
        }

        result.setSuccess(true);
        logConsumer.accept("========================================");
        logConsumer.accept("Pipeline completed: " + pipeline.getName());
        logConsumer.accept("========================================");

        return result;
    }

    /**
     * 执行单个步骤
     */
    private static StepResult executeStep(PipelineStep step, ExecutionContext context) {
        switch (step.getType()) {
            case LOCAL_COMMAND:
                return LocalCommandExecutor.execute(step, context);
            case UPLOAD:
                return UploadExecutor.execute(step, context);
            case REMOTE_COMMAND:
                return RemoteCommandExecutor.execute(step, context);
            default:
                return StepResult.failure("Unknown step type: " + step.getType());
        }
    }
}
```

- [ ] **Step 3.5: 创建 PipelineResult 类**

创建 `src/main/java/io/github/yueryou/easydev/plugin/model/PipelineResult.java`:

```java
package io.github.yueryou.easydev.plugin.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 流水线执行结果
 */
public class PipelineResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 失败的步骤
     */
    private PipelineStep failedStep;

    /**
     * 跳过的步骤
     */
    private List<PipelineStep> skippedSteps = new ArrayList<>();

    /**
     * 步骤执行结果映射（按顺序）
     */
    private Map<PipelineStep, StepResult> stepResults = new LinkedHashMap<>();

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
```

- [ ] **Step 3.6: 验证编译**

运行命令：
```bash
./gradlew compileJava
```
预期：编译成功，无错误

- [ ] **Step 3.7: 提交**

```bash
git add src/main/java/io/github/yueryou/easydev/plugin/executor/*.java \
        src/main/java/io/github/yueryou/easydev/plugin/model/PipelineResult.java
git commit -m "feat(pipeline): add pipeline executor and step executors

- Add PipelineExecutor for orchestrating pipeline execution
- Add LocalCommandExecutor for local command execution
- Add UploadExecutor for file upload operations
- Add RemoteCommandExecutor for remote SSH command execution
- Add PipelineResult for execution result tracking
- Support starting from specified step index
- Support failure strategies (STOP/CONTINUE)"
```

---

### Task 4: 创建流水线持久化类

**Files:**
- Create: `src/main/java/io/github/yueryou/easydev/plugin/model/PipelineConfigPersistence.java`
- Modify: `src/main/java/tech/lin2j/idea/plugin/model/ConfigPersistence.java`

- [ ] **Step 4.1: 扩展 ConfigPersistence 添加流水线支持**

修改 `src/main/java/tech/lin2j/idea/plugin/model/ConfigPersistence.java`，添加：

```java
// 在类中添加以下字段和 getter/setter

private List<io.github.yueryou.easydev.plugin.model.Pipeline> pipelines;

public List<io.github.yueryou.easydev.plugin.model.Pipeline> getPipelines() {
    if (pipelines == null) {
        pipelines = new java.util.concurrent.CopyOnWriteArrayList<>();
    }
    checkUid(pipelines);
    return pipelines;
}

public void setPipelines(List<io.github.yueryou.easydev.plugin.model.Pipeline> pipelines) {
    this.pipelines = pipelines;
}
```

- [ ] **Step 4.2: 创建 PipelineConfigPersistence 工具类**

创建 `src/main/java/io/github/yueryou/easydev/plugin/model/PipelineConfigPersistence.java`:

```java
package io.github.yueryou.easydev.plugin.model;

import com.intellij.openapi.application.ApplicationManager;
import tech.lin2j.idea.plugin.model.ConfigPersistence;

import java.util.List;
import java.util.UUID;

/**
 * 流水线配置持久化工具类
 */
public class PipelineConfigPersistence {

    // 禁止实例化
    private PipelineConfigPersistence() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 获取所有流水线
     */
    public static List<Pipeline> getAllPipelines() {
        ConfigPersistence persistence = ApplicationManager.getApplication().getService(ConfigPersistence.class);
        return persistence.getPipelines();
    }

    /**
     * 根据 ID 获取流水线
     */
    public static Pipeline getPipelineById(Integer id) {
        return getAllPipelines().stream()
                .filter(p -> p.getId() != null && p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据 UID 获取流水线
     */
    public static Pipeline getPipelineByUid(String uid) {
        return getAllPipelines().stream()
                .filter(p -> uid.equals(p.getUid()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 添加流水线
     */
    public static void addPipeline(Pipeline pipeline) {
        ConfigPersistence persistence = ApplicationManager.getApplication().getService(ConfigPersistence.class);

        // 生成 ID 和 UID
        int maxId = getAllPipelines().stream()
                .map(Pipeline::getId)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);

        pipeline.setId(maxId + 1);
        if (pipeline.getUid() == null) {
            pipeline.setUid(UUID.randomUUID().toString());
        }

        persistence.getPipelines().add(pipeline);
    }

    /**
     * 更新流水线
     */
    public static void updatePipeline(Pipeline pipeline) {
        // 更新操作由调用者直接修改对象属性后自动生效
        // 因为 ConfigPersistence 使用的是引用
    }

    /**
     * 删除流水线
     */
    public static void removePipeline(Pipeline pipeline) {
        ConfigPersistence persistence = ApplicationManager.getApplication().getService(ConfigPersistence.class);
        persistence.getPipelines().remove(pipeline);
    }

    /**
     * 获取最大流水线 ID
     */
    public static Integer maxPipelineId() {
        return getAllPipelines().stream()
                .map(Pipeline::getId)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
    }
}
```

- [ ] **Step 4.3: 扩展 ConfigHelper 添加流水线相关方法**

在 `src/main/java/tech/lin2j/idea/plugin/model/ConfigHelper.java` 中添加：

```java
// 添加以下静态方法

private static List<Pipeline> PIPELINE_LIST;

// 在 refreshConfig() 方法中添加
PIPELINE_LIST = CONFIG_PERSISTENCE.getPipelines();

// 添加公共方法

public static List<Pipeline> getAllPipelines() {
    ensureConfigLoadInMemory();
    return PIPELINE_LIST;
}

public static Pipeline getPipelineById(Integer id) {
    ensureConfigLoadInMemory();
    return PIPELINE_LIST.stream()
            .filter(p -> Objects.equals(p.getId(), id))
            .findFirst()
            .orElse(null);
}

public static Pipeline getPipelineByUid(String uid) {
    ensureConfigLoadInMemory();
    return PIPELINE_LIST.stream()
            .filter(p -> Objects.equals(p.getUid(), uid))
            .findFirst()
            .orElse(null);
}

public static void addPipeline(Pipeline pipeline) {
    ensureConfigLoadInMemory();
    CONFIG_PERSISTENCE.getPipelines().add(pipeline);
    PIPELINE_LIST = CONFIG_PERSISTENCE.getPipelines();
}

public static void removePipeline(Pipeline pipeline) {
    ensureConfigLoadInMemory();
    CONFIG_PERSISTENCE.getPipelines().remove(pipeline);
    PIPELINE_LIST = CONFIG_PERSISTENCE.getPipelines();
}

public static Integer maxPipelineId() {
    ensureConfigLoadInMemory();
    return PIPELINE_LIST.stream()
            .map(Pipeline::getId)
            .filter(Objects::nonNull)
            .max(Integer::compareTo)
            .orElse(0);
}
```

- [ ] **Step 4.4: 验证编译**

运行命令：
```bash
./gradlew compileJava
```
预期：编译成功，无错误

- [ ] **Step 4.5: 提交**

```bash
git add src/main/java/tech/lin2j/idea/plugin/model/ConfigPersistence.java \
        src/main/java/tech/lin2j/idea/plugin/model/ConfigHelper.java \
        src/main/java/io/github/yueryou/easydev/plugin/model/PipelineConfigPersistence.java
git commit -m "feat(pipeline): add pipeline persistence support

- Add pipelines field to ConfigPersistence
- Add PipelineConfigPersistence utility class
- Add pipeline CRUD methods to ConfigHelper
- Pipelines are stored in deploy-helper-settings.xml alongside other configs"
```

---

## Chunk 2: UI 组件和对话框

### Task 5: 创建 CommandPipelinePanel 主面板

**Files:**
- Modify: `src/main/java/io/github/yueryou/easydev/plugin/ui/component/CommandPipelinePanel.java`

- [ ] **Step 5.1: 重写 CommandPipelinePanel**

完全重写 `src/main/java/io/github/yueryou/easydev/plugin/ui/component/CommandPipelinePanel.java`:

```java
package io.github.yueryou.easydev.plugin.ui.component;

import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import io.github.yueryou.easydev.plugin.model.Pipeline;
import io.github.yueryou.easydev.plugin.model.PipelineConfigPersistence;
import io.github.yueryou.easydev.plugin.ui.dialog.PipelineEditDialog;
import io.github.yueryou.easydev.plugin.ui.render.PipelineListCellRenderer;
import io.github.yueryou.easydev.plugin.uitl.UiUtil;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.service.impl.PluginNotificationService;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义任务流水线面板
 */
public class CommandPipelinePanel extends JPanel {

    private final Project project;
    private final JPanel root;

    /**
     * 搜索输入框
     */
    private JBTextField searchInput;

    /**
     * 流水线列表
     */
    private JBList<Pipeline> pipelineList;

    /**
     * 已选择的流水线
     */
    private transient Pipeline selectedPipeline;

    private final PluginNotificationService notificationService;

    public CommandPipelinePanel(Project project) {
        this.project = project;
        this.notificationService = ApplicationManager.getApplication().getService(PluginNotificationService.class);

        initInput();
        initPipelineList();
        bindInputChangeListener(loadPipelineList());

        root = FormBuilder.createFormBuilder()
                .addLabeledComponent("搜索流水线", searchInput)
                .addComponentFillVertically(createPipelineToolbarPanel(), 8)
                .getPanel();
        root.setPreferredSize(new Dimension(UiUtil.screenWidth() / 2, 600));
    }

    private void initInput() {
        searchInput = new JBTextField();
        searchInput.getEmptyText().setText("输入流水线名称...");
    }

    private void initPipelineList() {
        pipelineList = new JBList<>();
        pipelineList.setCellRenderer(new PipelineListCellRenderer());
        pipelineList.addListSelectionListener(e -> {
            Pipeline pipeline = pipelineList.getSelectedValue();
            if (pipeline != null) {
                selectedPipeline = pipeline;
            } else {
                selectedPipeline = null;
            }
        });
    }

    private JPanel createPipelineToolbarPanel() {
        return ToolbarDecorator.createDecorator(pipelineList)
                .setToolbarPosition(ActionToolbarPosition.TOP)
                .disableUpDownActions()
                .setAddAction(e -> {
                    Pipeline pipeline = new Pipeline();
                    new PipelineEditDialog(project, pipeline).showAndGet();
                    loadPipelineList();
                })
                .setEditAction(e -> {
                    Pipeline pipeline = pipelineList.getSelectedValue();
                    if (pipeline != null) {
                        new PipelineEditDialog(project, pipeline).showAndGet();
                        loadPipelineList();
                    }
                })
                .setRemoveAction(e -> {
                    Pipeline pipeline = pipelineList.getSelectedValue();
                    if (pipeline == null) {
                        return;
                    }
                    boolean confirm = UiUtil.deleteConfirm(pipeline.getName());
                    if (confirm) {
                        PipelineConfigPersistence.removePipeline(pipeline);
                        loadPipelineList();
                    }
                })
                .addExtraAction(new RunPipelineAction())
                .createPanel();
    }

    private void bindInputChangeListener(List<Pipeline> pipelines) {
        searchInput.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                String text = searchInput.getText();
                if (text.isBlank()) {
                    pipelineList.setListData(pipelines.toArray(new Pipeline[0]));
                } else {
                    List<Pipeline> searchList = pipelines.stream()
                            .filter(pipeline -> pipeline.getName().contains(text))
                            .toList();
                    pipelineList.setListData(searchList.toArray(new Pipeline[0]));
                }
            }
        });
    }

    public List<Pipeline> loadPipelineList() {
        List<Pipeline> pipelines = PipelineConfigPersistence.getAllPipelines();
        pipelineList.setListData(pipelines.toArray(new Pipeline[0]));
        return pipelines;
    }

    public JPanel createUI() {
        return root;
    }

    /**
     * 运行流水线操作
     */
    private class RunPipelineAction extends JPanel {
        public RunPipelineAction() {
            super(new BorderLayout());
            JButton runButton = new JButton("运行", com.intellij.icons.AllIcons.Actions.Execute);
            runButton.addActionListener(e -> executePipeline());
            add(runButton, BorderLayout.CENTER);
        }
    }

    private void executePipeline() {
        if (selectedPipeline == null) {
            notificationService.showNotification(project, "运行流水线", "请先选择一个流水线");
            return;
        }

        if (selectedPipeline.getServerId() == null) {
            notificationService.showNotification(project, "运行流水线", "流水线未配置服务器");
            return;
        }

        if (selectedPipeline.getSteps() == null || selectedPipeline.getSteps().isEmpty()) {
            notificationService.showNotification(project, "运行流水线", "流水线没有步骤");
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "运行流水线：" + selectedPipeline.getName()) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                // TODO: 实现流水线执行逻辑
                notificationService.showNotification(project, "运行流水线", "功能开发中...");
            }
        });
    }
}
```

- [ ] **Step 5.2: 创建 PipelineListCellRenderer**

创建 `src/main/java/io/github/yueryou/easydev/plugin/ui/render/PipelineListCellRenderer.java`:

```java
package io.github.yueryou.easydev.plugin.ui.render;

import io.github.yueryou.easydev.plugin.model.Pipeline;

import javax.swing.*;
import java.awt.*;

/**
 * 流水线列表单元格渲染器
 */
public class PipelineListCellRenderer extends JLabel implements ListCellRenderer<Pipeline> {

    public PipelineListCellRenderer() {
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends Pipeline> list,
            Pipeline pipeline,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        if (pipeline == null) {
            setText("");
            return this;
        }

        setText(pipeline.getName());
        setIcon(com.intellij.icons.AllIcons.Actions.Execute);

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
    }
}
```

- [ ] **Step 5.3: 验证编译**

运行命令：
```bash
./gradlew compileJava
```
预期：编译成功，无错误

- [ ] **Step 5.4: 提交**

```bash
git add src/main/java/io/github/yueryou/easydev/plugin/ui/component/CommandPipelinePanel.java \
        src/main/java/io/github/yueryou/easydev/plugin/ui/render/PipelineListCellRenderer.java
git commit -m "feat(pipeline): add CommandPipelinePanel UI component

- Rewrite CommandPipelinePanel with pipeline list and toolbar
- Add search functionality
- Add PipelineListCellRenderer for list item display
- Add RunPipelineAction placeholder
- Follow IntelliJ Platform UI style guidelines"
```

---

### Task 6: 创建 PipelineEditDialog 编辑对话框

**Files:**
- Create: `src/main/java/io/github/yueryou/easydev/plugin/ui/dialog/PipelineEditDialog.java`
- Create: `src/main/java/io/github/yueryou/easydev/plugin/ui/dialog/StepEditDialog.java`

- [ ] **Step 6.1: 创建 PipelineEditDialog**

创建 `src/main/java/io/github/yueryou/easydev/plugin/ui/dialog/PipelineEditDialog.java`:

```java
package io.github.yueryou.easydev.plugin.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.FormBuilder;
import io.github.yueryou.easydev.plugin.model.FailureStrategy;
import io.github.yueryou.easydev.plugin.model.Pipeline;
import io.github.yueryou.easydev.plugin.model.PipelineConfigPersistence;
import io.github.yueryou.easydev.plugin.model.PipelineStep;
import io.github.yueryou.easydev.plugin.ui.render.PipelineStepListCellRenderer;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.SshServer;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 流水线编辑对话框
 */
public class PipelineEditDialog extends DialogWrapper {

    private final Project project;
    private final Pipeline pipeline;

    private JTextField nameField;
    private JBComboBox<String> serverComboBox;
    private JBComboBox<FailureStrategy> failureStrategyComboBox;
    private JBList<PipelineStep> stepList;
    private DefaultListModel<PipelineStep> stepListModel;

    public PipelineEditDialog(Project project, Pipeline pipeline) {
        super(project);
        this.project = project;
        this.pipeline = pipeline;

        setTitle(pipeline.getId() != null ? "编辑流水线" : "新建流水线");

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        nameField = new JTextField(30);
        if (pipeline.getName() != null) {
            nameField.setText(pipeline.getName());
        }

        // 服务器选择
        List<String> serverItems = new ArrayList<>();
        List<SshServer> servers = ConfigHelper.sshServers();
        serverItems.add(""); // 空选项
        for (SshServer server : servers) {
            serverItems.add(server.getId() + " - " + server.getHost() + ":" + server.getPort());
        }
        serverComboBox = new JBComboBox<>(serverItems.toArray(new String[0]));
        if (pipeline.getServerId() != null) {
            String selectedValue = pipeline.getServerId() + " - ";
            for (SshServer server : servers) {
                if (server.getId().equals(pipeline.getServerId())) {
                    selectedValue = server.getId() + " - " + server.getHost() + ":" + server.getPort();
                    break;
                }
            }
            serverComboBox.setSelectedItem(selectedValue);
        }

        // 失败策略选择
        failureStrategyComboBox = new JBComboBox<>(FailureStrategy.values());
        if (pipeline.getOnFailure() != null) {
            failureStrategyComboBox.setSelectedItem(pipeline.getOnFailure());
        }

        // 步骤列表
        stepListModel = new DefaultListModel<>();
        if (pipeline.getSteps() != null) {
            for (PipelineStep step : pipeline.getSteps()) {
                stepListModel.addElement(step);
            }
        }
        stepList = new JBList<>(stepListModel);
        stepList.setCellRenderer(new PipelineStepListCellRenderer());

        JPanel stepToolbarPanel = ToolbarDecorator.createDecorator(stepList)
                .setAddAction(e -> addStep())
                .setEditAction(e -> editStep())
                .setRemoveAction(e -> removeStep())
                .setMoveUpAction(e -> moveStepUp())
                .setMoveDownAction(e -> moveStepDown())
                .createPanel();

        return FormBuilder.createFormBuilder()
                .addLabeledComponent("名称", nameField)
                .addLabeledComponent("服务器", serverComboBox)
                .addLabeledComponent("失败策略", failureStrategyComboBox)
                .addLabeledComponent("步骤", stepToolbarPanel, true)
                .getPanel();
    }

    @Override
    protected void doOKAction() {
        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            Messages.showErrorDialog("请输入流水线名称", "验证失败");
            return;
        }

        String serverSelectedItem = (String) serverComboBox.getSelectedItem();
        Integer serverId = null;
        if (serverSelectedItem != null && !serverSelectedItem.isEmpty()) {
            String[] parts = serverSelectedItem.split(" - ");
            if (parts.length > 0) {
                try {
                    serverId = Integer.parseInt(parts[0]);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (serverId == null) {
            Messages.showErrorDialog("请选择服务器", "验证失败");
            return;
        }

        FailureStrategy failureStrategy = (FailureStrategy) failureStrategyComboBox.getSelectedItem();

        // 保存配置
        pipeline.setName(name.trim());
        pipeline.setServerId(serverId);
        pipeline.setOnFailure(failureStrategy);

        // 收集步骤
        List<PipelineStep> steps = new ArrayList<>();
        for (int i = 0; i < stepListModel.size(); i++) {
            steps.add(stepListModel.getElementAt(i));
        }
        pipeline.setSteps(steps);

        if (pipeline.getId() == null) {
            PipelineConfigPersistence.addPipeline(pipeline);
        } else {
            PipelineConfigPersistence.updatePipeline(pipeline);
        }

        super.doOKAction();
    }

    private void addStep() {
        StepEditDialog dialog = new StepEditDialog(project, null);
        if (dialog.showAndGet()) {
            PipelineStep step = dialog.getStep();
            stepListModel.addElement(step);
        }
    }

    private void editStep() {
        PipelineStep selectedStep = stepList.getSelectedValue();
        if (selectedStep == null) {
            return;
        }

        StepEditDialog dialog = new StepEditDialog(project, selectedStep);
        if (dialog.showAndGet()) {
            int index = stepList.getSelectedIndex();
            stepListModel.set(index, dialog.getStep());
        }
    }

    private void removeStep() {
        PipelineStep selectedStep = stepList.getSelectedValue();
        if (selectedStep == null) {
            return;
        }

        int confirm = Messages.showYesNoDialog(
                "确定要删除步骤 \"" + selectedStep.getName() + "\" 吗？",
                "确认删除",
                Messages.getQuestionIcon()
        );

        if (confirm == Messages.YES) {
            stepListModel.removeElement(selectedStep);
        }
    }

    private void moveStepUp() {
        int index = stepList.getSelectedIndex();
        if (index > 0) {
            PipelineStep step = stepListModel.remove(index);
            stepListModel.add(index - 1, step);
            stepList.setSelectedIndex(index - 1);
        }
    }

    private void moveStepDown() {
        int index = stepList.getSelectedIndex();
        if (index >= 0 && index < stepListModel.size() - 1) {
            PipelineStep step = stepListModel.remove(index);
            stepListModel.add(index + 1, step);
            stepList.setSelectedIndex(index + 1);
        }
    }
}
```

- [ ] **Step 6.2: 创建 PipelineStepListCellRenderer**

创建 `src/main/java/io/github/yueryou/easydev/plugin/ui/render/PipelineStepListCellRenderer.java`:

```java
package io.github.yueryou.easydev.plugin.ui.render;

import io.github.yueryou.easydev.plugin.model.LocalCommandStep;
import io.github.yueryou.easydev.plugin.model.PipelineStep;
import io.github.yueryou.easydev.plugin.model.RemoteCommandStep;
import io.github.yueryou.easydev.plugin.model.StepType;
import io.github.yueryou.easydev.plugin.model.UploadStep;

import javax.swing.*;
import java.awt.*;

/**
 * 流水线步骤列表单元格渲染器
 */
public class PipelineStepListCellRenderer extends JLabel implements ListCellRenderer<PipelineStep> {

    public PipelineStepListCellRenderer() {
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends PipelineStep> list,
            PipelineStep step,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        if (step == null) {
            setText("");
            return this;
        }

        StringBuilder text = new StringBuilder();
        text.append(index + 1).append(". ");
        text.append(step.getName() != null ? step.getName() : "未命名");
        text.append(" (");

        switch (step.getType()) {
            case LOCAL_COMMAND:
                text.append("本地命令");
                if (step instanceof LocalCommandStep) {
                    String cmd = ((LocalCommandStep) step).getCommand();
                    if (cmd != null && cmd.length() > 30) {
                        cmd = cmd.substring(0, 30) + "...";
                    }
                    text.append(": ").append(cmd);
                }
                break;
            case UPLOAD:
                text.append("上传文件");
                if (step instanceof UploadStep) {
                    text.append(": profile=").append(((UploadStep) step).getUploadProfileId());
                }
                break;
            case REMOTE_COMMAND:
                text.append("远程命令");
                if (step instanceof RemoteCommandStep) {
                    String cmd = ((RemoteCommandStep) step).getCommand();
                    if (cmd != null && cmd.length() > 30) {
                        cmd = cmd.substring(0, 30) + "...";
                    }
                    text.append(": ").append(cmd);
                }
                break;
        }
        text.append(")");

        setText(text.toString());
        setIcon(getIconForStepType(step.getType()));

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
    }

    private Icon getIconForStepType(StepType type) {
        switch (type) {
            case LOCAL_COMMAND:
                return com.intellij.icons.AllIcons.Terminal.Console;
            case UPLOAD:
                return com.intellij.icons.AllIcons.Actions.Upload;
            case REMOTE_COMMAND:
                return com.intellij.icons.AllIcons.Actions.Execute;
            default:
                return com.intellij.icons.AllIcons.General.Information;
        }
    }
}
```

- [ ] **Step 6.3: 验证编译**

运行命令：
```bash
./gradlew compileJava
```
预期：编译成功，无错误

- [ ] **Step 6.4: 提交**

```bash
git add src/main/java/io/github/yueryou/easydev/plugin/ui/dialog/PipelineEditDialog.java \
        src/main/java/io/github/yueryou/easydev/plugin/ui/render/PipelineStepListCellRenderer.java
git commit -m "feat(pipeline): add PipelineEditDialog for creating and editing pipelines

- Add PipelineEditDialog with form for pipeline configuration
- Add server selection dropdown
- Add failure strategy selection
- Add step list with add/edit/remove/move actions
- Add PipelineStepListCellRenderer for step display"
```

---

### Task 7: 创建 StepEditDialog 步骤编辑对话框

**Files:**
- Create: `src/main/java/io/github/yueryou/easydev/plugin/ui/dialog/StepEditDialog.java`

- [ ] **Step 7.1: 创建 StepEditDialog**

创建 `src/main/java/io/github/yueryou/easydev/plugin/ui/dialog/StepEditDialog.java`:

```java
package io.github.yueryou.easydev.plugin.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBComboBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import io.github.yueryou.easydev.plugin.model.LocalCommandStep;
import io.github.yueryou.easydev.plugin.model.PipelineStep;
import io.github.yueryou.easydev.plugin.model.RemoteCommandStep;
import io.github.yueryou.easydev.plugin.model.StepType;
import io.github.yueryou.easydev.plugin.model.UploadStep;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.model.Command;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.UploadProfile;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 步骤编辑对话框
 */
public class StepEditDialog extends DialogWrapper {

    private final Project project;
    private final PipelineStep existingStep;

    private JBComboBox<StepType> typeComboBox;
    private JTextField nameField;

    // Local command fields
    private JTextField commandField;
    private JTextField localWorkingDirField;
    private JTextField timeoutField;
    private JBComboBox<String> localCommandComboBox;

    // Upload fields
    private JBComboBox<String> uploadProfileComboBox;
    private JCheckBox createRemoteDirCheckBox;

    // Remote command fields
    private JTextField remoteCommandField;
    private JTextField remoteWorkingDirField;
    private JBComboBox<String> remoteCommandComboBox;

    private JPanel cardsPanel;
    private CardLayout cardLayout;

    public StepEditDialog(Project project, PipelineStep existingStep) {
        super(project);
        this.project = project;
        this.existingStep = existingStep;

        setTitle(existingStep != null ? "编辑步骤" : "添加步骤");

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        nameField = new JTextField(30);

        typeComboBox = new JBComboBox<>(StepType.values());
        typeComboBox.addActionListener(e -> onTypeChanged());

        // 本地命令组件
        commandField = new JTextField(30);
        localWorkingDirField = new JTextField(30);
        timeoutField = new JTextField("0", 10);

        // 上传组件
        List<String> profileItems = new ArrayList<>();
        profileItems.add("");
        for (UploadProfile profile : ConfigHelper.getAllUploadProfiles()) {
            profileItems.add(profile.getId() + " - " + profile.getName());
        }
        uploadProfileComboBox = new JBComboBox<>(profileItems.toArray(new String[0]));
        createRemoteDirCheckBox = new JCheckBox("创建远程目录", true);

        // 远程命令组件
        remoteCommandField = new JTextField(30);
        remoteWorkingDirField = new JTextField(30);

        // 命令选择下拉框（复用已有命令）
        List<String> commandItems = new ArrayList<>();
        commandItems.add("");
        for (Command cmd : ConfigHelper.getAllCommands()) {
            commandItems.add(cmd.getId() + " - " + cmd.getTitle());
        }
        localCommandComboBox = new JBComboBox<>(commandItems.toArray(new String[0]));
        remoteCommandComboBox = new JBComboBox<>(commandItems.toArray(new String[0]));

        // 卡片面板
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);

        cardsPanel.add(createLocalCommandPanel(), "LOCAL_COMMAND");
        cardsPanel.add(createUploadPanel(), "UPLOAD");
        cardsPanel.add(createRemoteCommandPanel(), "REMOTE_COMMAND");

        // 如果是编辑模式，填充数据
        if (existingStep != null) {
            nameField.setText(existingStep.getName());
            typeComboBox.setSelectedItem(existingStep.getType());
            populateFieldsFromStep(existingStep);
        }

        onTypeChanged();

        return FormBuilder.createFormBuilder()
                .addLabeledComponent("步骤类型", typeComboBox)
                .addLabeledComponent("步骤名称", nameField)
                .addLabeledComponent("配置", cardsPanel, true)
                .getPanel();
    }

    private JPanel createLocalCommandPanel() {
        return FormBuilder.createFormBuilder()
                .addLabeledComponent("命令", commandField)
                .addLabeledComponent("工作目录", localWorkingDirField)
                .addLabeledComponent("超时时间 (秒，0=不限制)", timeoutField)
                .addLabeledComponent("或使用已有命令", localCommandComboBox)
                .getPanel();
    }

    private JPanel createUploadPanel() {
        return FormBuilder.createFormBuilder()
                .addLabeledComponent("上传配置", uploadProfileComboBox)
                .addComponent(createRemoteDirCheckBox)
                .getPanel();
    }

    private JPanel createRemoteCommandPanel() {
        return FormBuilder.createFormBuilder()
                .addLabeledComponent("命令", remoteCommandField)
                .addLabeledComponent("工作目录", remoteWorkingDirField)
                .addLabeledComponent("或使用已有命令", remoteCommandComboBox)
                .getPanel();
    }

    private void onTypeChanged() {
        StepType selectedType = (StepType) typeComboBox.getSelectedItem();
        if (selectedType != null) {
            cardLayout.show(cardsPanel, selectedType.name());
        }
    }

    private void populateFieldsFromStep(PipelineStep step) {
        switch (step.getType()) {
            case LOCAL_COMMAND:
                LocalCommandStep localStep = (LocalCommandStep) step;
                commandField.setText(localStep.getCommand());
                localWorkingDirField.setText(localStep.getWorkingDir());
                timeoutField.setText(String.valueOf(localStep.getTimeout() != null ? localStep.getTimeout() : 0));
                if (localStep.getCommandId() != null) {
                    selectCommandInComboBox(localCommandComboBox, localStep.getCommandId());
                }
                break;
            case UPLOAD:
                UploadStep uploadStep = (UploadStep) step;
                selectProfileInComboBox(uploadProfileComboBox, uploadStep.getUploadProfileId());
                createRemoteDirCheckBox.setSelected(uploadStep.getCreateRemoteDir() != null ? uploadStep.getCreateRemoteDir() : true);
                break;
            case REMOTE_COMMAND:
                RemoteCommandStep remoteStep = (RemoteCommandStep) step;
                remoteCommandField.setText(remoteStep.getCommand());
                remoteWorkingDirField.setText(remoteStep.getWorkingDir());
                if (remoteStep.getCommandId() != null) {
                    selectCommandInComboBox(remoteCommandComboBox, remoteStep.getCommandId());
                }
                break;
        }
    }

    private void selectCommandInComboBox(JBComboBox<String> comboBox, Integer commandId) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            String item = comboBox.getItemAt(i);
            if (item != null && item.startsWith(commandId + " - ")) {
                comboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private void selectProfileInComboBox(JBComboBox<String> comboBox, Integer profileId) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            String item = comboBox.getItemAt(i);
            if (item != null && item.startsWith(profileId + " - ")) {
                comboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    @Override
    protected void doOKAction() {
        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(
                    getContentPane(),
                    "请输入步骤名称",
                    "验证失败",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        StepType type = (StepType) typeComboBox.getSelectedItem();
        PipelineStep step = createStepFromFields(type);
        step.setName(name.trim());

        // 将步骤存储在对话框中供获取
        ((PipelineStepHolder) this).setStep(step);

        super.doOKAction();
    }

    private PipelineStep createStepFromFields(StepType type) {
        switch (type) {
            case LOCAL_COMMAND:
                LocalCommandStep localStep = new LocalCommandStep();
                localStep.setCommand(commandField.getText());
                localStep.setWorkingDir(localWorkingDirField.getText());

                String timeoutText = timeoutField.getText();
                try {
                    int timeout = Integer.parseInt(timeoutText);
                    localStep.setTimeout(timeout > 0 ? timeout : null);
                } catch (NumberFormatException e) {
                    localStep.setTimeout(null);
                }

                // 检查是否选择了已有命令
                String selectedCommand = (String) localCommandComboBox.getSelectedItem();
                if (selectedCommand != null && !selectedCommand.isEmpty()) {
                    String[] parts = selectedCommand.split(" - ");
                    if (parts.length > 0) {
                        try {
                            localStep.setCommandId(Integer.parseInt(parts[0]));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }

                return localStep;

            case UPLOAD:
                UploadStep uploadStep = new UploadStep();
                String selectedProfile = (String) uploadProfileComboBox.getSelectedItem();
                if (selectedProfile != null && !selectedProfile.isEmpty()) {
                    String[] parts = selectedProfile.split(" - ");
                    if (parts.length > 0) {
                        try {
                            uploadStep.setUploadProfileId(Integer.parseInt(parts[0]));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                uploadStep.setCreateRemoteDir(createRemoteDirCheckBox.isSelected());
                return uploadStep;

            case REMOTE_COMMAND:
                RemoteCommandStep remoteStep = new RemoteCommandStep();
                remoteStep.setCommand(remoteCommandField.getText());
                remoteStep.setWorkingDir(remoteWorkingDirField.getText());

                // 检查是否选择了已有命令
                String selectedRemoteCommand = (String) remoteCommandComboBox.getSelectedItem();
                if (selectedRemoteCommand != null && !selectedRemoteCommand.isEmpty()) {
                    String[] parts = selectedRemoteCommand.split(" - ");
                    if (parts.length > 0) {
                        try {
                            remoteStep.setCommandId(Integer.parseInt(parts[0]));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }

                return remoteStep;

            default:
                throw new IllegalArgumentException("Unknown step type: " + type);
        }
    }

    public PipelineStep getStep() {
        return ((PipelineStepHolder) this).getStep();
    }

    // 内部接口用于存储步骤
    private interface PipelineStepHolder {
        PipelineStep getStep();
        void setStep(PipelineStep step);
    }

    private PipelineStep stepResult;

    public void setStep(PipelineStep step) {
        this.stepResult = step;
    }
}
```

- [ ] **Step 7.2: 验证编译**

运行命令：
```bash
./gradlew compileJava
```
预期：编译成功，无错误

- [ ] **Step 7.3: 提交**

```bash
git add src/main/java/io/github/yueryou/easydev/plugin/ui/dialog/StepEditDialog.java
git commit -m "feat(pipeline): add StepEditDialog for creating and editing pipeline steps

- Add StepEditDialog with card layout for different step types
- Support LocalCommandStep with command, working dir, timeout
- Support UploadStep with profile selection
- Support RemoteCommandStep with command and working dir
- Allow selecting existing commands from dropdown
- Validate input before saving"
```

---

## Chunk 3: 执行引擎集成和测试

### Task 8: 集成流水线执行功能

**Files:**
- Modify: `src/main/java/io/github/yueryou/easydev/plugin/ui/component/CommandPipelinePanel.java`

- [ ] **Step 8.1: 实现 executePipeline 方法**

修改 `CommandPipelinePanel.java` 中的 `executePipeline` 方法：

```java
private void executePipeline() {
    executePipelineFromStep(0);
}

private void executePipelineFromStep(int startIndex) {
    if (selectedPipeline == null) {
        notificationService.showNotification(project, "运行流水线", "请先选择一个流水线");
        return;
    }

    if (selectedPipeline.getServerId() == null) {
        notificationService.showNotification(project, "运行流水线", "流水线未配置服务器");
        return;
    }

    if (selectedPipeline.getSteps() == null || selectedPipeline.getSteps().isEmpty()) {
        notificationService.showNotification(project, "运行流水线", "流水线没有步骤");
        return;
    }

    ProgressManager.getInstance().run(new Task.Backgroundable(project, "运行流水线：" + selectedPipeline.getName()) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) {
            try {
                // 获取服务器配置
                tech.lin2j.idea.plugin.ssh.SshServer server =
                    ConfigHelper.getSshServerById(selectedPipeline.getServerId());

                if (server == null) {
                    notificationService.showNotification(project, "运行流水线", "未找到服务器配置");
                    return;
                }

                // 构建日志输出
                StringBuilder logOutput = new StringBuilder();
                java.util.function.Consumer<String> logConsumer = logOutput::append;

                // 执行流水线
                io.github.yueryou.easydev.plugin.model.PipelineResult result =
                    io.github.yueryou.easydev.plugin.executor.PipelineExecutor.executeFromStep(
                        selectedPipeline,
                        server,
                        project,
                        logConsumer,
                        startIndex
                    );

                // 显示结果
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (result.isSuccess()) {
                        notificationService.showNotification(
                            project,
                            "流水线执行成功",
                            selectedPipeline.getName()
                        );
                    } else {
                        notificationService.showNotification(
                            project,
                            "流水线执行失败",
                            "失败步骤：" + (result.getFailedStep() != null ? result.getFailedStep().getName() : "未知")
                        );
                    }
                });

            } catch (Exception e) {
                notificationService.showNotification(project, "运行流水线", "执行异常：" + e.getMessage());
            }
        }
    });
}
```

- [ ] **Step 8.2: 提交**

```bash
git add src/main/java/io/github/yueryou/easydev/plugin/ui/component/CommandPipelinePanel.java
git commit -m "feat(pipeline): integrate pipeline execution logic

- Implement executePipeline method with ProgressManager
- Add error handling for missing server/steps
- Display execution results via notification service
- Support starting from specified step index"
```

---

### Task 9: 添加右键菜单支持（从指定步骤执行）

**Files:**
- Modify: `src/main/java/io/github/yueryou/easydev/plugin/ui/component/CommandPipelinePanel.java`

- [ ] **Step 9.1: 添加步骤列表右键菜单**

由于我们需要在步骤级别提供右键菜单，需要修改 UI 结构，在流水线编辑对话框中添加步骤列表的右键菜单支持。这个功能可以在后续迭代中实现。

暂时跳过，在后续迭代中实现。

- [ ] **Step 9.2: 提交**

此功能留待后续迭代实现。

---

### Task 10: 添加 i18n 支持

**Files:**
- Modify: `src/main/resources/messages.properties` (如果存在)
- Modify: `src/main/resources/messages_en.properties`
- Modify: `src/main/resources/messages_zh.properties`

- [ ] **Step 10.1: 添加英文消息**

在 `src/main/resources/messages_en.properties` 中添加：

```properties
# Pipeline
pipeline.list.title=Pipelines
pipeline.edit.title=Edit Pipeline
pipeline.edit.name=Name:
pipeline.edit.server=Server:
pipeline.edit.failure.strategy=Failure Strategy:
pipeline.edit.steps=Steps:
pipeline.step.edit.title=Edit Step
pipeline.step.add.title=Add Step
pipeline.step.type=Step Type:
pipeline.step.name=Step Name:
pipeline.step.local.command=Command:
pipeline.step.working.dir=Working Directory:
pipeline.step.timeout=Timeout (seconds):
pipeline.step.upload.profile=Upload Profile:
pipeline.step.create.remote.dir=Create Remote Directory
pipeline.action.run=Run
pipeline.action.run.from=Run From This Step
pipeline.action.run.only=Run Only This Step
```

- [ ] **Step 10.2: 添加中文消息**

在 `src/main/resources/messages_zh.properties` 中添加：

```properties
# Pipeline
pipeline.list.title=流水线
pipeline.edit.title=编辑流水线
pipeline.edit.name=名称：
pipeline.edit.server=服务器：
pipeline.edit.failure.strategy=失败策略：
pipeline.edit.steps=步骤：
pipeline.step.edit.title=编辑步骤
pipeline.step.add.title=添加步骤
pipeline.step.type=步骤类型：
pipeline.step.name=步骤名称：
pipeline.step.local.command=命令：
pipeline.step.working.dir=工作目录：
pipeline.step.timeout=超时时间 (秒)：
pipeline.step.upload.profile=上传配置：
pipeline.step.create.remote.dir=创建远程目录
pipeline.action.run=运行
pipeline.action.run.from=从此步骤开始执行
pipeline.action.run.only=仅执行此步骤
```

- [ ] **Step 10.3: 提交**

```bash
git add src/main/resources/messages_en.properties \
        src/main/resources/messages_zh.properties
git commit -m "feat(pipeline): add i18n support for pipeline feature

- Add English messages for pipeline UI
- Add Chinese messages for pipeline UI
- Support localization of step types and actions"
```

---

## 实现完成检查清单

完成所有任务后，执行以下检查：

### 功能检查
- [ ] 可以创建新流水线
- [ ] 可以编辑已有流水线
- [ ] 可以删除流水线
- [ ] 可以添加步骤（3 种类型）
- [ ] 可以编辑步骤
- [ ] 可以删除步骤
- [ ] 可以调整步骤顺序
- [ ] 步骤可以选择已有命令
- [ ] 可以运行流水线
- [ ] 流水线执行结果正确显示

### 代码质量检查
- [ ] 所有 Java 文件编译通过
- [ ] 遵循 IntelliJ Platform UI 规范
- [ ] 使用 JBLabel/JBList/JBTextField 等组件
- [ ] 使用 FormBuilder/ToolbarDecorator
- [ ] 对话框继承 DialogWrapper
- [ ] 代码有适当的注释

### 数据持久化检查
- [ ] 流水线配置正确保存
- [ ] 重新加载后配置正确恢复
- [ ] 支持导出/导入配置（通过现有 ConfigImportExport）

---

## 后续迭代建议

以下功能可在后续迭代中实现：

1. **右键菜单从指定步骤执行** - 在步骤列表上右键弹出菜单
2. **执行进度可视化** - 在执行详情面板中显示每个步骤的状态
3. **执行历史记录** - 保存每次执行的日志和历史
4. **变量自动补全** - 在命令输入时提供变量建议
5. **步骤复制/粘贴** - 快速复制已有步骤
6. **流水线导入/导出** - 支持分享和备份流水线配置
7. **执行前变量预览** - 在执行前预览将使用的变量值
8. **步骤条件执行** - 基于上一步结果决定是否执行

---

**计划完成。Ready to execute?**