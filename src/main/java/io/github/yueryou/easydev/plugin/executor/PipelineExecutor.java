package io.github.yueryou.easydev.plugin.executor;

import com.intellij.openapi.project.Project;
import io.github.yueryou.easydev.plugin.model.ExecutionContext;
import io.github.yueryou.easydev.plugin.model.FailureStrategy;
import io.github.yueryou.easydev.plugin.model.Pipeline;
import io.github.yueryou.easydev.plugin.model.PipelineResult;
import io.github.yueryou.easydev.plugin.model.PipelineStep;
import io.github.yueryou.easydev.plugin.model.RemoteCommandStep;
import io.github.yueryou.easydev.plugin.model.StepResult;
import io.github.yueryou.easydev.plugin.model.StepType;
import io.github.yueryou.easydev.plugin.model.UploadStep;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.ssh.SshServer;

import java.util.List;
import java.util.function.Consumer;

/**
 * 流水线主执行器
 */
public class PipelineExecutor {

    private PipelineExecutor() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 执行完整流水线
     *
     * @param pipeline    流水线配置
     * @param server      SSH 服务器
     * @param project     项目
     * @param logConsumer 日志消费者
     * @return 流水线执行结果
     */
    public static PipelineResult execute(Pipeline pipeline, SshServer server, Project project, Consumer<String> logConsumer) {
        return executeFromStep(pipeline, server, project, logConsumer, 0);
    }

    /**
     * 从指定步骤开始执行流水线
     *
     * @param pipeline    流水线配置
     * @param server      SSH 服务器
     * @param project     项目
     * @param logConsumer 日志消费者
     * @param startIndex  起始步骤索引
     * @return 流水线执行结果
     */
    public static PipelineResult executeFromStep(
            Pipeline pipeline,
            SshServer server,
            Project project,
            Consumer<String> logConsumer,
            int startIndex) {

        PipelineResult result = new PipelineResult();
        List<PipelineStep> steps = pipeline.getSteps();

        logConsumer.accept("========== 流水线开始：" + pipeline.getName() + " ==========");
        logConsumer.accept("失败策略：" + pipeline.getOnFailure());
        logConsumer.accept("从步骤 " + startIndex + " 开始执行");

        // 创建执行上下文
        ExecutionContext context = new ExecutionContext(pipeline, server, project, logConsumer);
        context.setStartIndex(startIndex);

        // 遍历步骤列表
        for (int i = 0; i < steps.size(); i++) {
            PipelineStep step = steps.get(i);

            // 跳过 null 步骤
            if (step == null) {
                logConsumer.accept("[警告] 步骤 " + i + " 为 null，已跳过");
                continue;
            }

            // 跳过 startIndex 之前的步骤
            if (i < startIndex) {
                continue;
            }

            // 检查步骤是否启用
            if (!step.isEnabled()) {
                logConsumer.accept("[跳过] 步骤已禁用：" + step.getName());
                result.addSkippedStep(step);
                continue;
            }

            logConsumer.accept("");
            logConsumer.accept("----- 执行步骤 " + (i + 1) + "/" + steps.size() + ": " + step.getName() + " -----");

            // 执行步骤
            StepResult stepResult = executeStep(step, context);

            // 保存步骤执行结果
            result.addStepResult(step, stepResult);
            context.addStepOutput(step.getUid(), stepResult);

            // 检查失败策略
            if (!stepResult.isSuccess()) {
                logConsumer.accept("步骤执行失败：" + step.getName());

                if (pipeline.getOnFailure() == FailureStrategy.STOP) {
                    logConsumer.accept("失败策略为 STOP，停止执行后续步骤");
                    result.setSuccess(false);
                    result.setFailedStep(step);

                    // 标记剩余步骤为已跳过
                    for (int j = i + 1; j < steps.size(); j++) {
                        result.addSkippedStep(steps.get(j));
                    }
                    return result;
                } else {
                    logConsumer.accept("失败策略为 CONTINUE，继续执行后续步骤");
                }
            }
        }

        // 检查整体结果
        boolean allSuccess = result.getStepResults().values().stream()
                .filter(r -> r != null)
                .allMatch(StepResult::isSuccess);

        result.setSuccess(allSuccess);
        if (!allSuccess && result.getFailedStep() == null) {
            // 查找失败的步骤
            for (PipelineStep step : steps) {
                StepResult stepResult = result.getStepResult(step);
                if (stepResult != null && !stepResult.isSuccess()) {
                    result.setFailedStep(step);
                    break;
                }
            }
        }

        logConsumer.accept("");
        logConsumer.accept("========== 流水线结束：" + (allSuccess ? "成功" : "失败") + " ==========");

        return result;
    }

    /**
     * 执行单个步骤
     *
     * @param step    步骤
     * @param context 执行上下文
     * @return 步骤执行结果
     */
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
                    context.getLogConsumer().accept("[错误] Upload 步骤未配置 Server: " + step.getName());
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
                    context.getLogConsumer().accept("[错误] RemoteCommand 步骤未配置 Server: " + step.getName());
                    return StepResult.failure("RemoteCommand 步骤未配置 Server");
                }
            default:
                return StepResult.failure("不支持的步骤类型：" + step.getType());
        }
    }
}
