# CommandPipelinePanel 用户自定义流水线功能 - 需求规格文档

**文档版本：** 1.0
**创建日期：** 2026-03-27
**状态：** 待评审

---

## 1. 概述

### 1.1 目标

在 `CommandPipelinePanel` 中实现用户自定义流水线功能，允许用户编排多个任务步骤并按顺序执行，实现自动化部署、巡检等运维场景。

### 1.2 核心场景（优先实现）

**构建 + 部署流程**：用户运行流水线任务后，自动执行以下步骤：
1. 调用构建命令，执行打包动作
2. 打包成功后，调用文件上传功能上传文件
3. 执行远程命令重启服务

---

## 2. 功能需求

### 2.1 流水线管理

| 功能 | 描述 |
|------|------|
| 创建流水线 | 通过工具栏 [+] 按钮创建新流水线 |
| 编辑流水线 | 通过工具栏 [编辑] 按钮修改已有流水线 |
| 删除流水线 | 通过工具栏 [删除] 按钮删除选中流水线 |
| 运行流水线 | 通过工具栏 [运行] 按钮执行选中流水线 |
| 设置 | 通过工具栏 [设置] 按钮配置全局选项 |

### 2.2 流水线结构

每个流水线包含以下元素：

| 字段 | 类型 | 描述 |
|------|------|------|
| `name` | String | 流水线名称，如 "构建 + 部署到测试环境" |
| `serverId` | Integer | 目标 SSH 服务器 ID |
| `steps` | List<Step> | 步骤列表，按顺序执行 |
| `onFailure` | Enum | 失败策略：STOP(默认) / CONTINUE |

### 2.3 步骤类型（Phase 1）

优先实现 3 种步骤类型，预留扩展性：

#### 2.3.1 本地命令步骤 (LocalCommandStep)

| 字段 | 类型 | 描述 |
|------|------|------|
| `name` | String | 步骤名称，用于显示 |
| `command` | String | 要执行的本地命令 |
| `workingDir` | String | 可选，工作目录 |
| `timeout` | Integer | 可选，超时时间（秒） |

**输出变量：**
- `{{stepN.stdout}}` - 命令标准输出
- `{{stepN.exitCode}}` - 退出码

#### 2.3.2 上传文件步骤 (UploadStep)

| 字段 | 类型 | 描述 |
|------|------|------|
| `name` | String | 步骤名称，用于显示 |
| `uploadProfileId` | Integer | 关联的 UploadProfile ID |
| `createRemoteDir` | Boolean | 是否创建远程目录 |

**输出变量：**
- `{{stepN.uploadedFiles}}` - 上传的文件列表
- `{{stepN.remotePath}}` - 远程目标路径

#### 2.3.3 远程命令步骤 (RemoteCommandStep)

| 字段 | 类型 | 描述 |
|------|------|------|
| `name` | String | 步骤名称，用于显示 |
| `command` | String | 要执行的远程命令 |
| `workingDir` | String | 可选，远程工作目录 |
| `commandId` | Integer | 可选，关联已有 Command ID |

**输出变量：**
- `{{stepN.stdout}}` - 命令输出
- `{{stepN.exitCode}}` - 退出码

### 2.4 执行控制

| 特性 | 描述 |
|------|------|
| 串行执行 | 步骤按顺序一个一个执行 |
| 失败即停 | 默认情况下，某一步失败后停止执行后续步骤 |
| 从指定步骤执行 | 右键菜单选择"从此步骤开始执行" |
| 仅执行此步骤 | 右键菜单选择"仅执行此步骤" |

### 2.5 变量系统

#### 2.5.1 变量语法

使用模板风格：`{{VAR_NAME}}`

示例：
```
上传文件：{{workspace}}/build/libs/app.jar
远程命令：java -jar {{step2.remotePath}}/app.jar
```

#### 2.5.2 预设变量

**系统变量（所有流水线可用）：**
| 变量名 | 说明 | 示例值 |
|--------|------|--------|
| `{{timestamp}}` | 执行时间戳 | `20260327_100000` |
| `{{workspace}}` | 项目根目录 | `/path/to/project` |
| `{{user.home}}` | 用户主目录 | `/home/user` |

**步骤输出变量（由步骤类型自动提供）：**
| 步骤类型 | 变量名 | 说明 |
|----------|--------|------|
| 本地命令 | `{{stepN.stdout}}` | 命令标准输出 |
| 本地命令 | `{{stepN.exitCode}}` | 退出码 |
| 上传文件 | `{{stepN.uploadedFiles}}` | 上传的文件列表 |
| 上传文件 | `{{stepN.remotePath}}` | 远程目标路径 |
| 远程命令 | `{{stepN.stdout}}` | 命令输出 |
| 远程命令 | `{{stepN.exitCode}}` | 退出码 |

---

## 3. UI/UX 设计

### 3.0 IDEA UI 统一风格规范

**设计原则：** 遵循 IntelliJ Platform UI 设计规范，确保与 IDE 整体风格一致。

#### 组件选型规范

| 组件 | IntelliJ 平台组件 | 说明 |
|------|------------------|------|
| 标签 | `JBLabel` | 不使用 `JLabel` |
| 按钮 | `JButton` / `ActionButton` | 工具栏使用 `ActionButton` |
| 文本框 | `JBTextField` | 不使用 `JTextField` |
| 列表 | `JBList` | 不使用 `JList` |
| 面板 | `JPanel` + `GridBagLayout` / `BorderLayout` | 使用 IntelliJ 布局管理器 |
| 对话框 | `JDialog` + `JPanel` | 使用 `DialogWrapper` 包装 |
| 复选框 | `JCheckBox` / `JBCheckBox` | 使用 `JBCheckBox` |
| 下拉框 | `JBComboBox` | 不使用 `JComboBox` |

#### 间距规范

| 场景 | 间距值 | 说明 |
|------|--------|------|
| 表单组件间距 | 8px | 使用 `JBInsets` |
| 对话框边距 | 10px | 标准边距 |
| 组件组间距 | 16px | 逻辑分组之间 |
| 工具栏间距 | 4px | 按钮之间 |

#### 颜色规范

| 用途 | 颜色值 | IntelliJ 颜色常量 |
|------|--------|------------------|
| 成功状态 | `#57A843` | `JBColor.GREEN` |
| 失败状态 | `#F75555` | `JBColor.RED` |
| 执行中 | `#389FD6` | `JBColor.BLUE` |
| 跳过状态 | `#9AA0A6` | `JBColor.GRAY` |
| 等待状态 | `#D0D0D0` | 浅灰色 |

#### 字体规范

| 用途 | 字体大小 | 字体样式 |
|------|----------|----------|
| 普通文本 | 12pt | `JBFont.label()` |
| 标题 | 14pt | `JBFont.label().asBold()` |
| 小字说明 | 10pt | `JBFont.label().asSmall()` |
| 代码/路径 | 11pt | `JBFont.label().asBold()` |

#### 图标规范

| 状态 | 图标 | IntelliJ 内置图标 |
|------|------|------------------|
| 等待中 | ○ | `AllIcons.General.InlineExclude` |
| 执行中 | ▶ | `AllIcons.Actions.Execute` |
| 成功 | ✓ | `AllIcons.Actions.GreenCheck` |
| 失败 | ✗ | `AllIcons.Actions.Red_x` |
| 跳过 | ⊘ | `AllIcons.General.Exclude` |
| 添加 | + | `AllIcons.General.Add` |
| 编辑 | ✎ | `AllIcons.Actions.Edit` |
| 删除 | 🗑 | `AllIcons.General.Remove` |
| 运行 | ▶ | `AllIcons.Actions.Execute` |

#### 对话框规范

- 使用 `DialogWrapper` 作为基类
- 对话框宽度：最小 400px，推荐 600px
- 对话框高度：根据内容自适应，最大不超过屏幕高度的 80%
- 按钮位置：右下角，`[确定]` 在左，`[取消]` 在右
- 默认焦点：`[确定]` 按钮
- 支持 `Enter` 键确认，`Esc` 键取消

#### 表单布局规范

使用 `FormBuilder` 或 `GridBagLayout`：
```java
// 推荐方式 - FormBuilder
JPanel panel = FormBuilder.createFormBuilder()
    .addLabeledComponent("名称：", nameField)
    .addLabeledComponent("服务器：", serverCombo)
    .addComponentFillVertically(stepsPanel, 0)
    .getPanel();
```

#### 工具栏规范

使用 `ToolbarDecorator`：
```java
ToolbarDecorator.createDecorator(list)
    .setToolbarPosition(ActionToolbarPosition.TOP)
    .setAddAction(e -> addNewItem())
    .setEditAction(e -> editItem())
    .setRemoveAction(e -> removeItem())
    .createPanel();
```

---

### 3.1 主面板布局

```
┌─────────────────────────────────────────────┐
│ [+] 运行  [编辑]  [删除]           [设置]   │ ← 工具栏
├─────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────┐ │
│ │ ▶ 构建 + 部署到测试环境                  │ │ ← 流水线列表
│ │ ▶ 一键巡检                               │ │   (可多选)
│ │ ▶ 快速拉取日志                           │ │
│ └─────────────────────────────────────────┘ │
├─────────────────────────────────────────────┤
│ 执行详情 ▼                                  │ ← 可折叠面板
│ ┌─────────────────────────────────────────┐ │
│ │ [✓] 步骤 1: 构建命令     1.2s           │ │
│ │ [✓] 步骤 2: 上传文件     3.4s           │ │
│ │ [▶] 步骤 3: 重启服务     执行中...      │ │
│ │ [ ] 步骤 4: 等待后续步骤                │ │
│ └─────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────┐ │
│ │ 2026-03-27 10:00:01 开始执行            │ │ ← 日志输出区
│ │ 2026-03-27 10:00:02 [步骤 1] 正在执行... │ │
│ │ 2026-03-27 10:00:03 [步骤 1] 执行成功   │ │
│ │ 2026-03-27 10:00:04 [步骤 2] 正在上传... │ │
│ └─────────────────────────────────────────┘ │
└─────────────────────────────────────────────┘
```

### 3.2 执行状态图标

使用 IntelliJ 平台内置图标（通过 `AllIcons` 访问）：

| 状态 | 图标 | IntelliJ 图标常量 | 颜色 |
|------|------|------------------|------|
| 等待中 | ○ | `AllIcons.General.InlineExclude` | 灰色 |
| 执行中 | ▶ | `AllIcons.Actions.Execute` | 蓝色 |
| 成功 | ✓ | `AllIcons.Actions.GreenCheck` | 绿色 |
| 失败 | ✗ | `AllIcons.Actions.Red_x` | 红色 |
| 已跳过 | ⊘ | `AllIcons.General.Exclude` | 灰色 |

### 3.3 右键菜单（从指定步骤执行）

在步骤列表上右键点击某个步骤，弹出 `JBPopupMenu`：

```java
JBPopupMenu popupMenu = new JBPopupMenu();
popupMenu.add(new AnAction("从此步骤开始执行", null, AllIcons.Actions.Execute));
popupMenu.add(new AnAction("仅执行此步骤", null, AllIcons.Actions.StepInto));
popupMenu.addSeparator();
popupMenu.add(new AnAction("编辑步骤", null, AllIcons.Actions.Edit));
popupMenu.add(new AnAction("删除步骤", null, AllIcons.General.Remove));
popupMenu.addSeparator();
popupMenu.add(new AnAction("在上方插入步骤", null, AllIcons.General.Add));
popupMenu.add(new AnAction("在下方插入步骤", null, AllIcons.General.Add));
```

---

### 3.4 编辑流水线对话框

采用独立对话框模式，继承 `DialogWrapper`，类似 `AddCommandDialog`：

**Java 类结构：**
```java
public class PipelineEditDialog extends DialogWrapper {
    // 使用 FormBuilder 构建表单
    // 工具栏使用 ToolbarDecorator
}
```

**布局示意：**
```
┌─────────────────────────────────────────────┐
│ 编辑流水线                                 │
├─────────────────────────────────────────────┤
│ 名称：[构建 + 部署到测试环境____________]    │
│ 服务器：[测试服务器 (192.168.1.100) ▼]     │
│ 失败策略：[停止执行 ▼]                     │
├─────────────────────────────────────────────┤
│ 步骤列表                  [+] [编辑] [删除] │
│ ┌─────────────────────────────────────────┐ │
│ │ 1. [本地命令] 构建项目 (./gradlew ...)  │ │
│ │ 2. [上传文件] 上传构建产物 (app.jar)    │ │
│ │ 3. [远程命令] 重启服务 (systemctl ...)  │ │
│ └─────────────────────────────────────────┘ │
│      [↑] [↓]  (调整步骤顺序)                │
├─────────────────────────────────────────────┤
│              [确定]        [取消]           │
└─────────────────────────────────────────────┘
```

### 3.5 添加/编辑步骤对话框

继承 `DialogWrapper`，使用 `JBTabbedPane` 按步骤类型切换不同的配置面板：

**Java 类结构：**
```java
public class StepEditDialog extends DialogWrapper {
    // 使用 JBComboBox 选择步骤类型
    // 使用 FormBuilder 构建表单
    // 支持从已有 Command 选择（JBComboBox）
}
```

**布局示意：**
```
┌─────────────────────────────────────────────┐
│ 添加步骤                                   │
├─────────────────────────────────────────────┤
│ 步骤类型：[本地命令 ▼]                      │
├─────────────────────────────────────────────┤
│ 步骤名称：[构建项目______________________]  │
│ 命令：    [./gradlew clean build_________]  │
│ 工作目录：[D:\Project\my-app_____________]  │
│ 超时时间：[300] 秒（0 = 不限制）            │
│                                             │
│ ○ 使用已有命令：[选择命令 ▼]                │
├─────────────────────────────────────────────┤
│              [确定]        [取消]           │
└─────────────────────────────────────────────┘
```

---

### 3.6 组件使用速查表

| UI 元素 | 推荐组件 | 备注 |
|---------|----------|------|
| 流水线列表 | `JBList<Pipeline>` + `DefaultListModel` | 支持自定义 Renderer |
| 步骤列表 | `JBList<PipelineStep>` + `DefaultListModel` | 支持拖拽排序 |
| 工具栏 | `ToolbarDecorator` | 标准工具栏模式 |
| 表单 | `FormBuilder` / `GridBagLayout` | IntelliJ 风格表单 |
| 对话框 | `DialogWrapper` 子类 | 标准对话框模式 |
| 右键菜单 | `JBPopupMenu` | 轻量级弹出菜单 |
| 进度显示 | `JProgressBar` + `JBLabel` | 带状态文本 |
| 日志输出 | `JBTextArea` (只读) | 使用等宽字体 |

---

## 4. 数据模型设计

### 4.1 Pipeline（流水线）

```java
public class Pipeline implements UniqueModel {
    private Integer id;
    private String uid;
    private String name;              // 流水线名称
    private Integer serverId;         // 目标 SSH 服务器 ID
    private List<PipelineStep> steps; // 步骤列表
    private FailureStrategy onFailure; // 失败策略：STOP / CONTINUE
    private Date createdAt;
    private Date updatedAt;
}

public enum FailureStrategy {
    STOP,      // 失败即停止
    CONTINUE   // 继续执行
}
```

### 4.2 PipelineStep（步骤基类）

```java
public abstract class PipelineStep {
    protected String uid;
    protected String name;           // 步骤名称
    protected StepType type;         // 步骤类型
    protected Boolean enabled = true; // 是否启用

    // 执行方法，由子类实现
    public abstract StepResult execute(ExecutionContext context);
}

public enum StepType {
    LOCAL_COMMAND,    // 本地命令
    UPLOAD,           // 上传文件
    REMOTE_COMMAND    // 远程命令
}
```

### 4.3 LocalCommandStep

```java
public class LocalCommandStep extends PipelineStep {
    private String command;        // 命令内容
    private String workingDir;     // 工作目录
    private Integer timeout;       // 超时时间（秒）

    // 支持引用已有 Command
    private Integer commandId;     // 关联的 Command ID
}
```

### 4.4 UploadStep

```java
public class UploadStep extends PipelineStep {
    private Integer uploadProfileId;  // UploadProfile ID
    private Boolean createRemoteDir;  // 是否创建远程目录
}
```

### 4.5 RemoteCommandStep

```java
public class RemoteCommandStep extends PipelineStep {
    private String command;        // 命令内容
    private String workingDir;     // 远程工作目录

    // 支持引用已有 Command
    private Integer commandId;     // 关联的 Command ID
}
```

### 4.6 ExecutionContext（执行上下文）

```java
public class ExecutionContext {
    private final Pipeline pipeline;
    private final SshServer server;
    private final Map<String, Object> variables;  // 变量映射
    private final Project project;
    private final Consumer<String> logConsumer;   // 日志输出

    // 预设变量初始化
    public ExecutionContext(...) {
        variables.put("timestamp", generateTimestamp());
        variables.put("workspace", project.getBasePath());
        variables.put("user.home", System.getProperty("user.home"));
    }

    // 变量解析
    public String resolve(String template) {
        // 将 {{var.name}} 替换为实际值
    }

    // 添加步骤输出到上下文
    public void addStepOutput(String stepUid, StepResult result) {
        variables.put(stepUid + ".stdout", result.getStdout());
        variables.put(stepUid + ".exitCode", result.getExitCode());
        // ...
    }
}
```

### 4.7 StepResult（步骤执行结果）

```java
public class StepResult {
    private boolean success;
    private int exitCode;
    private String stdout;
    private String stderr;
    private String errorMessage;
    private Duration duration;
    private Map<String, Object> outputs;  // 步骤特定的输出
}
```

---

## 5. 持久化设计

### 5.1 存储位置

**独立配置文件** - 在项目配置目录（`.idea/`）下创建 `pipelines.json` 文件：

```
.idea/
  └── easy-deploy/
      └── pipelines.json
```

### 5.2 文件格式

```json
{
  "version": "1.0",
  "pipelines": [
    {
      "uid": "pipeline-uuid-1",
      "name": "构建 + 部署到测试环境",
      "serverId": 1,
      "onFailure": "STOP",
      "steps": [
        {
          "uid": "step-uuid-1",
          "type": "LOCAL_COMMAND",
          "name": "构建项目",
          "command": "./gradlew clean build",
          "workingDir": "",
          "timeout": 300
        },
        {
          "uid": "step-uuid-2",
          "type": "UPLOAD",
          "name": "上传构建产物",
          "uploadProfileId": 1,
          "createRemoteDir": true
        },
        {
          "uid": "step-uuid-3",
          "type": "REMOTE_COMMAND",
          "name": "重启服务",
          "command": "systemctl restart myapp",
          "commandId": 5
        }
      ]
    }
  ]
}
```

### 5.3 配置管理类

```java
public class PipelineConfigPersistence {
    // 加载配置
    public static List<Pipeline> loadPipelines(Project project);

    // 保存配置
    public static void savePipelines(Project project, List<Pipeline> pipelines);

    // 添加/更新/删除
    public static void addPipeline(Pipeline pipeline);
    public static void updatePipeline(Pipeline pipeline);
    public static void removePipeline(Pipeline pipeline);
}
```

---

## 6. 执行流程

### 6.1 正常执行流程

```
用户点击 [运行]
    ↓
创建 ExecutionContext，初始化预设变量
    ↓
遍历步骤列表（按顺序）
    ↓
对于每个步骤：
  1. 解析变量（替换 {{var}} 为实际值）
  2. 执行步骤
  3. 更新 UI 状态（执行中 → 成功/失败）
  4. 记录日志
  5. 将步骤输出添加到上下文
    ↓
检查执行结果
  - 成功：继续下一个步骤
  - 失败：检查 onFailure 策略
    - STOP: 停止执行
    - CONTINUE: 继续下一个步骤
    ↓
所有步骤完成，显示总结
```

### 6.2 从指定步骤执行

```
用户右键选择"从此步骤开始执行"
    ↓
标记起始步骤
    ↓
创建 ExecutionContext
    ↓
遍历步骤列表
    ↓
对于起始步骤之前的步骤：
  - 标记为"已跳过"
  - 不执行
    ↓
从起始步骤开始正常执行流程
```

---

## 7. 扩展性设计

### 7.1 新增步骤类型

未来添加新步骤类型时，只需：

1. 创建新的 Step 类继承 `PipelineStep`
2. 实现 `execute(ExecutionContext context)` 方法
3. 在 `StepType` 枚举中添加新类型
4. 在添加步骤对话框中添加对应的 UI 配置

### 7.2 未来可能的步骤类型

| 类型 | 描述 |
|------|------|
| DownloadStep | 从远程服务器下载文件 |
| NotifyStep | 发送通知（钉钉/企业微信/邮件） |
| WaitStep | 等待/延迟指定时间 |
| ConditionStep | 条件判断（基于上一步结果） |
| ScriptStep | 执行自定义脚本（Shell/Python/Groovy） |

---

## 8. 验收标准

### 8.1 功能验收

- [ ] 可以创建、编辑、删除流水线
- [ ] 可以添加、删除、调整步骤顺序
- [ ] 支持 3 种步骤类型：本地命令、上传文件、远程命令
- [ ] 步骤可以引用已有 Command
- [ ] 变量系统正常工作（{{var}} 语法）
- [ ] 串行执行，失败即停止
- [ ] 支持从指定步骤开始执行（右键菜单）
- [ ] 执行状态正确显示（等待/执行中/成功/失败/跳过）
- [ ] 日志输出正确显示

### 8.2 UI 验收

- [ ] 主面板布局符合设计
- [ ] 工具栏按钮功能正常
- [ ] 步骤状态图标正确显示
- [ ] 右键菜单功能正常
- [ ] 编辑对话框功能正常

### 8.3 数据验收

- [ ] 流水线配置正确保存到 `.idea/easy-deploy/pipelines.json`
- [ ] 重新打开项目后配置正确加载
- [ ] 导出/导入配置包含流水线数据

---

## 9. 待确认事项

以下为后续需要确认的设计决策：

1. **错误重试机制** - 是否需要支持步骤失败后自动重试？
2. **并发执行** - 未来是否需要支持同时部署到多台服务器？
3. **步骤参数化** - 是否需要支持步骤级别的参数配置（类似函数参数）？
4. **流水线模板** - 是否需要提供预置的流水线模板（如"一键部署 MySQL"）？

---

## 10. 参考资料

- 现有 Command 管理：`CommandManagePanel.java`
- 现有命令对话框：`AddCommandDialog.java`
- SSH 服务接口：`ISshService.java`
- 配置持久化：`ConfigPersistence.java`

---

**下一步：** 用户评审本需求规格文档 → 修改完善 → 调用 `writing-plans` skill 创建实现计划
