package tech.lin2j.idea.plugin.uitl;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.enums.Constant;
import tech.lin2j.idea.plugin.file.ConsoleTransferListener;
import tech.lin2j.idea.plugin.file.filter.ConsoleFileFilter;
import tech.lin2j.idea.plugin.file.filter.ExtExcludeFilter;
import tech.lin2j.idea.plugin.file.filter.RegexFileFilter;
import tech.lin2j.idea.plugin.model.Command;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.UploadProfile;
import tech.lin2j.idea.plugin.service.ISshService;
import tech.lin2j.idea.plugin.ssh.CommandLog;
import tech.lin2j.idea.plugin.ssh.SshConnectionManager;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.SshStatus;
import tech.lin2j.idea.plugin.ssh.sshj.SshjConnection;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.intellij.openapi.ui.DialogWrapper.OK_EXIT_CODE;

/**
 * @author linjinjia
 * @date 2022/5/7 08:58
 */
public class CommandUtil {

    public static void executeUpload(@NotNull Project project, @NotNull UploadProfile profile,
                                     @NotNull SshServer server, @NotNull DialogWrapper dialogWrapper) {
        closeDialog(dialogWrapper);
        showToolWindow(project);
        // Prevent blocking UI thread
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            CommandLog commandLog = project.getUserData(CommandLog.COMMAND_LOG_KEY);
            assert commandLog != null;
            executeUpload(profile, server, commandLog);
        });
    }

    public static void executeCommand(@NotNull Project project, Command command,
                                      SshServer server, DialogWrapper dialogWrapper) {
        if (dialogWrapper != null) {
            closeDialog(dialogWrapper);
        }
        showToolWindow(project);
        CommandLog commandLog = project.getUserData(CommandLog.COMMAND_LOG_KEY);
        assert commandLog != null;

        executeCommand(command, server, commandLog);
    }

    public static void executeUpload(UploadProfile profile, SshServer server, CommandLog commandLog) {
        String remoteTargetDir = profile.getLocation();
        String exclude = profile.getExclude();
        ConsoleFileFilter filter = new ConsoleFileFilter(new ExtExcludeFilter(exclude, commandLog), commandLog);
        try {
            SshjConnection sshjConnection = SshConnectionManager.makeSshjConnection(server);
            ISshService sshService = ApplicationManager.getApplication().getService(ISshService.class);
            
            // Execute pre-upload command if exists (synchronously)
            executeCommand(profile.getPreCommandId(), "pre-upload", profile, server, sshService, sshjConnection, commandLog, true);

            String[] localFiles = profile.getFile().split(Constant.LOCAL_FILE_SEPARATOR);

            printTransferMode(commandLog);
            boolean allUploaded = true;
            for (String localFile : localFiles) {
                String[] ss = localFile.split(Constant.LOCAL_FILE_INFO_SEPARATOR);
                String targetFile = ss[0];
                boolean useRegex = ss.length == 2 && Objects.equals(ss[1], Constant.STR_TRUE);
                if (useRegex) {
                    RegexFileFilter regexFilter = new RegexFileFilter(PathUtil.getFileName(targetFile), commandLog);
                    filter.addFilter(regexFilter);
                    targetFile = PathUtil.getParentPath(targetFile);
                }

                commandLog.info("Upload [" + targetFile + "] to [" + remoteTargetDir + "]" + (useRegex ? ", regex: true" : ""));
                sshjConnection.setTransferListener(new ConsoleTransferListener(targetFile, commandLog));
                boolean success = sshService.upload(filter, sshjConnection, targetFile, remoteTargetDir, commandLog, !useRegex);
                if (!success) {
                    allUploaded = false;
                    break;
                }

                if (useRegex) {
                    filter.remove(filter);
                }
            }

            boolean hasAsyncPostCommand = false;
            if (allUploaded) {
                // Handle backward compatibility: if preCommandId and postCommandId are null but commandId exists, migrate to postCommandId
                Integer postCommandId = profile.getPostCommandId();
                if (profile.getPreCommandId() == null && postCommandId == null && profile.getCommandId() != null) {
                    postCommandId = profile.getCommandId();
                }
                
                // Execute post-upload command if exists (asynchronously)
                if (postCommandId != null) {
                    hasAsyncPostCommand = true;
                    executeCommand(postCommandId, "post-upload", profile, server, sshService, sshjConnection, commandLog, false);
                }
            }
            
            printFinished(commandLog);
            // Only close connection in main thread if no async post command is running
            // Async post command will close the connection itself
            if (!hasAsyncPostCommand) {
                sshjConnection.close();
            }
        } catch (Exception e) {
            commandLog.error(e.getMessage());
        }
    }

    private static void executeCommand(Integer commandId, String timing, UploadProfile profile, SshServer server, 
                                     ISshService sshService, SshjConnection sshjConnection, CommandLog commandLog, boolean synchronous) {
        if (commandId == null) {
            return;
        }
        
        Command command = ConfigHelper.getCommandById(commandId);
        if (command == null) {
            return;
        }
        
        String cmdContent;
        if (profile.getUseUploadPath() != null && profile.getUseUploadPath()) {
            // Use upload target directory as command execution directory
            cmdContent = command.generateCmdLine(profile.getLocation());
        } else {
            // Use command's configured directory
            cmdContent = command.generateCmdLine();
        }
        
        commandLog.info(String.format("Execute %s command on %s:%s : {%s}", timing, server.getIp(), server.getPort(), cmdContent));
        
        // Execute command synchronously or asynchronously based on the synchronous parameter
        if (synchronous) {
            try {
                SshStatus result = sshjConnection.execute(cmdContent, commandLog);
                if (!result.isSuccess()) {
                    commandLog.error(String.format("%s command failed: %s", timing, result.getMessage()));
                    throw new RuntimeException(timing + " command failed: " + result.getMessage());
                }
                commandLog.info(timing + " command completed successfully");
            } catch (Exception e) {
                commandLog.error(String.format("%s command execution error: %s", timing, e.getMessage()));
                throw new RuntimeException(timing + " command execution error: " + e.getMessage());
            }
        } else {
            // Execute asynchronously
            sshService.executeAsync(commandLog, sshjConnection, cmdContent);
        }
    }

    public static void executeCommand(Command command, SshServer server, CommandLog commandLog) {
        String cmdContent = command.generateCmdLine();
        commandLog.info(String.format("Execute command on %s:%s : {%s}", server.getIp(), server.getPort(), cmdContent));
        ISshService sshService = ApplicationManager.getApplication().getService(ISshService.class);
        sshService.executeAsync(commandLog, server, cmdContent);
    }

    private static void showToolWindow(Project project) {
        ToolWindow deployToolWindow = ToolWindowManager.getInstance(project).getToolWindow("Easy Deploy");
        assert deployToolWindow != null;
        deployToolWindow.activate(null);
        Content messages = deployToolWindow.getContentManager().findContent("Console");
        deployToolWindow.getContentManager().setSelectedContent(messages);
    }

    private static void closeDialog(DialogWrapper dialogWrapper) {
        dialogWrapper.close(OK_EXIT_CODE);
    }

    private static void printFinished(CommandLog commandLog) {
        commandLog.info("Finished at: " + LocalDateTime.now());
    }


    private static void printTransferMode(CommandLog commandLog) {
        String mode = ConfigHelper.isSCPTransferMode() ? "SCP" : "SFTP";
        commandLog.info("Transfer in " + mode + " mode");
    }

}