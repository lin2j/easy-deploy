package io.github.yueryou.easydev.plugin.executor;

import io.github.yueryou.easydev.plugin.model.ExecutionContext;
import io.github.yueryou.easydev.plugin.model.PipelineStep;
import io.github.yueryou.easydev.plugin.model.StepResult;
import io.github.yueryou.easydev.plugin.model.UploadStep;
import tech.lin2j.idea.plugin.file.filter.FileFilter;
import tech.lin2j.idea.plugin.file.filter.RegexFileFilter;
import tech.lin2j.idea.plugin.model.Command;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.UploadProfile;
import tech.lin2j.idea.plugin.service.ISshService;
import tech.lin2j.idea.plugin.service.impl.SshjSshService;
import tech.lin2j.idea.plugin.ssh.CommandLog;
import tech.lin2j.idea.plugin.ssh.SshConnectionManager;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.SshStatus;
import tech.lin2j.idea.plugin.ssh.sshj.SshjConnection;

import java.io.File;
import java.util.concurrent.FutureTask;

/**
 * 上传文件执行器
 */
public class UploadExecutor {

    private UploadExecutor() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 创建 CommandLog 实例，用于捕获 SSH 操作日志
     *
     * @param context 执行上下文
     * @param prefix  日志前缀
     * @return CommandLog 实例
     */
    private static CommandLog createCommandLog(ExecutionContext context, String prefix) {
        return new CommandLog() {
            @Override
            public com.intellij.execution.ui.ConsoleView getConsole() {
                return null;
            }

            @Override
            public void print(String msg, com.intellij.execution.ui.ConsoleViewContentType contentType) {
                context.getLogConsumer().accept(prefix + msg);
            }

            @Override
            public void addTask(FutureTask<?> task) {
            }

            @Override
            public void deleteTask(FutureTask<?> task) {
            }

            @Override
            public void stopAllTasks() {
            }

            @Override
            public int taskNum() {
                return 0;
            }
        };
    }

    public static StepResult execute(PipelineStep step, ExecutionContext context, SshServer server) {
        if (!(step instanceof UploadStep)) {
            return StepResult.failure("Invalid step type: expected UploadStep");
        }

        UploadStep uploadStep = (UploadStep) step;
        context.getLogConsumer().accept("[Upload] 开始执行：" + uploadStep.getName());

        // 验证 Server 配置
        if (server == null) {
            return StepResult.failure("SSH 服务器未配置");
        }

        // 获取 UploadProfile
        String profileId = uploadStep.getUploadProfileId();
        if (profileId == null || profileId.isEmpty()) {
            return StepResult.failure("未配置上传 Profile ID");
        }

        UploadProfile profile = ConfigHelper.getAllUploadProfiles().stream()
                .filter(p -> profileId.equals(p.getId() != null ? String.valueOf(p.getId()) : p.getUid()))
                .findFirst()
                .orElse(null);

        if (profile == null) {
            return StepResult.failure("未找到 UploadProfile: " + profileId);
        }

        context.getLogConsumer().accept("[Upload] 使用上传配置：" + profile.getName());

        // 解析本地文件路径和远程目录（支持相对路径）
        // getFile() 可能包含 ::0 或 ::1 后缀，表示是否使用正则表达式过滤
        String fileValue = profile.getFile();
        String localFilePath;
        if (fileValue != null && fileValue.contains("::")) {
            // 移除 :: 后缀，只保留文件路径
            localFilePath = fileValue.split("::")[0];
        } else {
            localFilePath = fileValue;
        }
        String localFile = context.resolvePath(localFilePath);
        String remoteDir = context.resolve(profile.getLocation());

        // 检查远程目录是否为空
        if (remoteDir == null || remoteDir.trim().isEmpty()) {
            context.getLogConsumer().accept("[Upload] 错误：远程目录未配置，请在上传配置中设置目标路径");
            return StepResult.failure("远程目录未配置");
        }

        // 验证本地文件存在
        File file = new File(localFile);
        if (!file.exists()) {
            context.getLogConsumer().accept("[Upload] 本地文件不存在：" + localFile);
            return StepResult.failure("本地文件不存在：" + localFile);
        }

        context.getLogConsumer().accept("[Upload] 本地文件：" + localFile);
        context.getLogConsumer().accept("[Upload] 远程目录：" + remoteDir);

        // 执行前置命令（同步）
        if (profile.getPreCommandId() != null) {
            context.getLogConsumer().accept("[Upload] 执行前置命令...");
            StepResult preCommandResult = executeCommand(profile.getPreCommandId(), profile, server, context);
            if (!preCommandResult.isSuccess()) {
                context.getLogConsumer().accept("[Upload] 前置命令执行失败");
                return StepResult.failure("前置命令执行失败");
            }
        }

        try {
            ISshService sshService = new SshjSshService();
            SshjConnection connection = SshConnectionManager.makeSshjConnection(server);
            if (connection == null) {
                return StepResult.failure("SSH 连接建立失败");
            }

            // 执行上传
            boolean createRemoteDir = uploadStep.isCreateRemoteDir();
            context.getLogConsumer().accept("[Upload] 是否创建远程目录：" + createRemoteDir);

            CommandLog commandLog = createCommandLog(context, "[Upload] ");
            // 如果配置了 exclude 过滤规则，使用 RegexFileFilter；否则使用接受所有文件的默认 filter
            FileFilter fileFilter = profile.getExclude() != null && !profile.getExclude().isEmpty()
                    ? new RegexFileFilter(profile.getExclude(), commandLog)
                    : (filename) -> true;
            boolean success = sshService.upload(fileFilter, connection, localFile, remoteDir, commandLog, createRemoteDir);

            // 执行后置命令（同步）
            boolean postCommandSuccess = true;
            if (success && profile.getPostCommandId() != null) {
                context.getLogConsumer().accept("[Upload] 执行后置命令...");
                StepResult postCommandResult = executeCommand(profile.getPostCommandId(), profile, server, context);
                if (!postCommandResult.isSuccess()) {
                    context.getLogConsumer().accept("[Upload] 后置命令执行失败");
                    postCommandSuccess = false;
                }
            }

            connection.close();

            if (success && postCommandSuccess) {
                context.getLogConsumer().accept("[Upload] 上传成功");
                StepResult result = StepResult.success("文件上传成功", 0);
                result.addOutput("uploadedFile", localFile);
                result.addOutput("remotePath", remoteDir);
                return result;
            } else if (!success) {
                context.getLogConsumer().accept("[Upload] 上传失败");
                return StepResult.failure("文件上传失败");
            } else {
                context.getLogConsumer().accept("[Upload] 上传成功但后置命令失败");
                return StepResult.failure("后置命令执行失败");
            }

        } catch (Exception e) {
            context.getLogConsumer().accept("[Upload] 上传异常：" + e.getMessage());
            return StepResult.failure("上传异常：" + e.getMessage());
        }
    }

    /**
     * 执行命令
     *
     * @param commandId 命令 ID
     * @param profile   上传配置
     * @param server    SSH 服务器
     * @param context   执行上下文
     * @return 命令执行结果
     */
    private static StepResult executeCommand(Integer commandId, UploadProfile profile,
                                             SshServer server, ExecutionContext context) {
        try {
            Command command = ConfigHelper.getCommandById(commandId);
            if (command == null) {
                context.getLogConsumer().accept("[命令] 未找到命令：" + commandId);
                return StepResult.failure("未找到命令：" + commandId);
            }

            String cmdContent;
            if (profile.getUseUploadPath() != null && profile.getUseUploadPath()) {
                // 使用上传目录作为命令执行目录
                cmdContent = command.generateCmdLine(profile.getLocation());
            } else {
                // 使用命令配置的目录
                cmdContent = command.generateCmdLine();
            }

            context.getLogConsumer().accept("[命令] 执行命令：" + cmdContent);

            ISshService sshService = new SshjSshService();
            SshjConnection connection = SshConnectionManager.makeSshjConnection(server);
            if (connection == null) {
                context.getLogConsumer().accept("[命令] SSH 连接建立失败");
                return StepResult.failure("SSH 连接建立失败");
            }

            CommandLog commandLog = createCommandLog(context, "[命令] ");

            SshStatus result = connection.execute(cmdContent, commandLog);
            connection.close();

            if (!result.isSuccess()) {
                context.getLogConsumer().accept("[命令] 命令执行失败：" + result.getMessage());
                return StepResult.failure("命令执行失败：" + result.getMessage());
            }

            context.getLogConsumer().accept("[命令] 命令执行成功");
            return StepResult.success("命令执行成功", 0);

        } catch (Exception e) {
            context.getLogConsumer().accept("[命令] 命令执行异常：" + e.getMessage());
            return StepResult.failure("命令执行异常：" + e.getMessage());
        }
    }
}
