package io.github.yueryou.easydev.plugin.executor;

import io.github.yueryou.easydev.plugin.model.ExecutionContext;
import io.github.yueryou.easydev.plugin.model.PipelineStep;
import io.github.yueryou.easydev.plugin.model.StepResult;
import io.github.yueryou.easydev.plugin.model.UploadStep;
import tech.lin2j.idea.plugin.file.filter.FileFilter;
import tech.lin2j.idea.plugin.file.filter.RegexFileFilter;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.UploadProfile;
import tech.lin2j.idea.plugin.service.ISshService;
import tech.lin2j.idea.plugin.service.impl.SshjSshService;
import tech.lin2j.idea.plugin.ssh.CommandLog;
import tech.lin2j.idea.plugin.ssh.SshConnectionManager;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.sshj.SshjConnection;

import java.io.File;

/**
 * 上传文件执行器
 */
public class UploadExecutor {

    private UploadExecutor() {
        throw new IllegalStateException("Utility class");
    }

    public static StepResult execute(PipelineStep step, ExecutionContext context) {
        if (!(step instanceof UploadStep)) {
            return StepResult.failure("Invalid step type: expected UploadStep");
        }

        UploadStep uploadStep = (UploadStep) step;
        context.getLogConsumer().accept("[Upload] 开始执行：" + uploadStep.getName());

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

        // 解析本地文件路径（支持变量）
        String localFile = context.resolve(profile.getFile());
        String remoteDir = context.resolve(profile.getLocation());

        // 验证本地文件存在
        File file = new File(localFile);
        if (!file.exists()) {
            context.getLogConsumer().accept("[Upload] 本地文件不存在：" + localFile);
            return StepResult.failure("本地文件不存在：" + localFile);
        }

        context.getLogConsumer().accept("[Upload] 本地文件：" + localFile);
        context.getLogConsumer().accept("[Upload] 远程目录：" + remoteDir);

        try {
            // 获取 SSH 服务
            SshServer server = context.getServer();
            if (server == null) {
                return StepResult.failure("SSH 服务器未配置");
            }

            ISshService sshService = new SshjSshService();
            SshjConnection connection = SshConnectionManager.makeSshjConnection(server);
            if (connection == null) {
                return StepResult.failure("SSH 连接建立失败");
            }

            // 执行上传
            boolean createRemoteDir = uploadStep.isCreateRemoteDir();
            context.getLogConsumer().accept("[Upload] 是否创建远程目录：" + createRemoteDir);

            CommandLog commandLog = new CommandLog() {
                @Override
                public com.intellij.execution.ui.ConsoleView getConsole() {
                    return null;
                }

                @Override
                public void print(String msg, com.intellij.execution.ui.ConsoleViewContentType contentType) {
                    context.getLogConsumer().accept("[Upload] " + msg);
                }

                @Override
                public void addTask(java.util.concurrent.FutureTask<?> task) {
                }

                @Override
                public void deleteTask(java.util.concurrent.FutureTask<?> task) {
                }

                @Override
                public void stopAllTasks() {
                }

                @Override
                public int taskNum() {
                    return 0;
                }
            };
            // 如果配置了 exclude 过滤规则，使用 RegexFileFilter
            FileFilter fileFilter = null;
            if (profile.getExclude() != null && !profile.getExclude().isEmpty()) {
                fileFilter = new RegexFileFilter(profile.getExclude(), commandLog);
            }
            boolean success = sshService.upload(fileFilter, connection, localFile, remoteDir, commandLog, createRemoteDir);

            if (success) {
                context.getLogConsumer().accept("[Upload] 上传成功");
                StepResult result = StepResult.success("文件上传成功", 0);
                result.addOutput("uploadedFile", localFile);
                result.addOutput("remotePath", remoteDir);
                return result;
            } else {
                context.getLogConsumer().accept("[Upload] 上传失败");
                return StepResult.failure("文件上传失败");
            }

        } catch (Exception e) {
            context.getLogConsumer().accept("[Upload] 上传异常：" + e.getMessage());
            return StepResult.failure("上传异常：" + e.getMessage());
        }
    }
}
