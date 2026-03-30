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

    private RemoteCommandExecutor() {
        throw new IllegalStateException("Utility class");
    }

    public static StepResult execute(PipelineStep step, ExecutionContext context, SshServer server) {
        if (!(step instanceof RemoteCommandStep)) {
            return StepResult.failure("Invalid step type: expected RemoteCommandStep");
        }

        RemoteCommandStep remoteStep = (RemoteCommandStep) step;
        context.getLogConsumer().accept("[RemoteCommand] 开始执行：" + remoteStep.getName());

        // 验证 Server 配置
        if (server == null) {
            return StepResult.failure("SSH 服务器未配置");
        }

        // 解析变量
        String command = context.resolve(remoteStep.getCommand());
        String workingDir = remoteStep.getWorkingDir() != null ? context.resolve(remoteStep.getWorkingDir()) : null;

        // 如果配置了 commandId，使用已有命令
        if (remoteStep.getCommandId() != null && !remoteStep.getCommandId().isEmpty()) {
            Command cmd = ConfigHelper.getCommandById(Integer.parseInt(remoteStep.getCommandId()));
            if (cmd != null) {
                command = context.resolve(cmd.generateCmdLine(workingDir));
                context.getLogConsumer().accept("[RemoteCommand] 使用预定义命令：" + cmd.getTitle());
            } else {
                context.getLogConsumer().accept("[RemoteCommand] 警告：未找到 commandId=" + remoteStep.getCommandId() + " 的 command");
            }
        }

        context.getLogConsumer().accept("[RemoteCommand] 执行命令：" + command);

        try {
            ISshService sshService = new SshjSshService();

            // 执行远程命令（同步）
            long startTime = System.currentTimeMillis();
            SshStatus status = sshService.execute(server, command);
            long duration = System.currentTimeMillis() - startTime;

            if (status.isSuccess()) {
                context.getLogConsumer().accept("[RemoteCommand] 执行成功");
                StepResult result = StepResult.success(status.getMessage(), 0);
                result.setDuration(java.time.Duration.ofMillis(duration));
                return result;
            } else {
                context.getLogConsumer().accept("[RemoteCommand] 执行失败：" + status.getMessage());
                StepResult result = StepResult.failure("远程命令执行失败：" + status.getMessage());
                result.setStdout(status.getMessage());
                result.setDuration(java.time.Duration.ofMillis(duration));
                return result;
            }

        } catch (Exception e) {
            context.getLogConsumer().accept("[RemoteCommand] 执行异常：" + e.getMessage());
            return StepResult.failure("远程命令执行异常：" + e.getMessage());
        }
    }
}
