package io.github.yueryou.easydev.plugin.model;

import com.intellij.openapi.project.Project;
import io.github.yueryou.easydev.plugin.util.VariableResolver;
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
    private final Map<String, StepResult> stepOutputs;
    private int startIndex = 0;

    public ExecutionContext(Pipeline pipeline, SshServer server, Project project, Consumer<String> logConsumer) {
        this.pipeline = pipeline;
        this.server = server;
        this.project = project;
        this.logConsumer = logConsumer;
        this.variables = new HashMap<>();
        this.stepOutputs = new HashMap<>();
        initPredefinedVariables();
    }

    private void initPredefinedVariables() {
        // 时间戳
        variables.put("timestamp", String.valueOf(System.currentTimeMillis()));

        // 工作空间（项目 basePath）
        if (project != null && project.getBasePath() != null) {
            variables.put("workspace", project.getBasePath());
        }

        // 用户主目录
        variables.put("user.home", System.getProperty("user.home"));

        // 服务器信息
        if (server != null) {
            variables.put("server.host", server.getIp());
            variables.put("server.port", String.valueOf(server.getPort()));
            variables.put("server.username", server.getUsername());
        }

        // 格式化日期时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        variables.put("datetime", sdf.format(new Date()));

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        variables.put("date", sdfDate.format(new Date()));

        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
        variables.put("time", sdfTime.format(new Date()));
    }

    /**
     * 解析模板中的变量
     *
     * @param template 模板字符串
     * @return 解析后的字符串
     */
    public String resolve(String template) {
        return VariableResolver.resolve(template, variables);
    }

    /**
     * 添加步骤执行结果到上下文
     *
     * @param stepUid 步骤 UID
     * @param result  执行结果
     */
    public void addStepOutput(String stepUid, StepResult result) {
        stepOutputs.put(stepUid, result);
        // 将步骤输出变量添加到上下文中，支持跨步骤数据传递
        if (result != null && result.getOutputs() != null) {
            for (Map.Entry<String, Object> entry : result.getOutputs().entrySet()) {
                variables.put("step." + stepUid + "." + entry.getKey(), entry.getValue());
            }
        }
    }

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

    public Map<String, StepResult> getStepOutputs() {
        return stepOutputs;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * 获取步骤执行结果
     *
     * @param stepUid 步骤 UID
     * @return 步骤执行结果
     */
    public StepResult getStepOutput(String stepUid) {
        return stepOutputs.get(stepUid);
    }
}
