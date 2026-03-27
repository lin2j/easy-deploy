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

    private LocalCommandExecutor() {
        throw new IllegalStateException("Utility class");
    }

    public static StepResult execute(PipelineStep step, ExecutionContext context) {
        if (!(step instanceof LocalCommandStep)) {
            return StepResult.failure("Invalid step type: expected LocalCommandStep");
        }

        LocalCommandStep localStep = (LocalCommandStep) step;
        context.getLogConsumer().accept("[LocalCommand] 开始执行：" + localStep.getName());

        // 解析变量
        String command = context.resolve(localStep.getCommand());
        String workingDir = localStep.getWorkingDir() != null ? context.resolve(localStep.getWorkingDir()) : null;
        int timeout = localStep.getTimeout() > 0 ? localStep.getTimeout() : 300;

        // 如果配置了 commandId，使用已有命令
        if (localStep.getCommandId() != null && !localStep.getCommandId().isEmpty()) {
            Command cmd = ConfigHelper.getCommandById(Integer.parseInt(localStep.getCommandId()));
            if (cmd != null) {
                command = context.resolve(cmd.generateCmdLine(workingDir));
                context.getLogConsumer().accept("[LocalCommand] 使用预定义命令：" + cmd.getTitle());
            } else {
                context.getLogConsumer().accept("[LocalCommand] 警告：未找到 commandId=" + localStep.getCommandId() + " 的command");
            }
        }

        context.getLogConsumer().accept("[LocalCommand] 执行命令：" + command);

        long startTime = System.currentTimeMillis();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
                processBuilder.command("cmd.exe", "/c", command);
            } else {
                processBuilder.command("sh", "-c", command);
            }

            if (workingDir != null && !workingDir.trim().isEmpty()) {
                processBuilder.directory(new java.io.File(workingDir));
            }

            Process process = processBuilder.start();

            // 读取标准输出
            StringBuilder stdoutBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stdoutBuilder.append(line).append("\n");
                    context.getLogConsumer().accept("[LocalCommand] " + line);
                }
            }

            // 读取错误输出
            StringBuilder stderrBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stderrBuilder.append(line).append("\n");
                }
            }

            // 等待完成，支持超时
            if (!process.waitFor(timeout, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                StepResult result = StepResult.failure("命令执行超时（" + timeout + "秒）");
                result.setDuration(java.time.Duration.ofMillis(System.currentTimeMillis() - startTime));
                return result;
            }

            int exitCode = process.exitValue();
            long duration = System.currentTimeMillis() - startTime;

            if (exitCode == 0) {
                context.getLogConsumer().accept("[LocalCommand] 执行成功，退出码：" + exitCode);
                StepResult result = StepResult.success(stdoutBuilder.toString().trim(), exitCode);
                result.setDuration(java.time.Duration.ofMillis(duration));
                return result;
            } else {
                context.getLogConsumer().accept("[LocalCommand] 执行失败，退出码：" + exitCode);
                StepResult result = StepResult.failure("命令执行失败，退出码：" + exitCode);
                result.setExitCode(exitCode);
                result.setStdout(stdoutBuilder.toString().trim());
                result.setStderr(stderrBuilder.toString().trim());
                result.setDuration(java.time.Duration.ofMillis(duration));
                return result;
            }

        } catch (IOException e) {
            context.getLogConsumer().accept("[LocalCommand] 执行异常：" + e.getMessage());
            StepResult result = StepResult.failure("IO 异常：" + e.getMessage());
            result.setDuration(java.time.Duration.ofMillis(System.currentTimeMillis() - startTime));
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            context.getLogConsumer().accept("[LocalCommand] 执行中断");
            StepResult result = StepResult.failure("命令执行被中断");
            result.setDuration(java.time.Duration.ofMillis(System.currentTimeMillis() - startTime));
            return result;
        }
    }
}
