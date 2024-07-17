package tech.lin2j.idea.plugin.uitl;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.model.Command;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.UploadProfile;
import tech.lin2j.idea.plugin.model.event.CommandExecuteEvent;
import tech.lin2j.idea.plugin.model.event.UploadProfileExecuteEvent;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.service.ISshService;
import tech.lin2j.idea.plugin.factory.SshServiceFactory;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.SshStatus;

import static com.intellij.openapi.ui.DialogWrapper.OK_EXIT_CODE;

/**
 * @author linjinjia
 * @date 2022/5/7 08:58
 */
public class CommandUtil {

    private static final ISshService sshService = SshServiceFactory.getSshService();

    public static void executeAndShowMessages(Project project, Command command, UploadProfile profile,
                                              SshServer server, DialogWrapper dialogWrapper) {
        dialogWrapper.close(OK_EXIT_CODE);

        String title;
        Command cmd;
        if (profile != null && profile.getCommandId() != null) {
            cmd = ConfigHelper.getCommandById(profile.getCommandId());
            title = String.format("Uploading file to %s:%s", server.getIp(), server.getPort());
        } else {
            cmd = command;
            title = String.format("Executing command on %s:%s", server.getIp(), server.getPort());
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(project, title) {
            final UploadProfileExecuteEvent uploadEvent = new UploadProfileExecuteEvent();
            final CommandExecuteEvent commandEvent = new CommandExecuteEvent(project, cmd, server, null);

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(false);
                uploadEvent.setSuccess(true);
                uploadEvent.setNeedExecCommand(true);
                commandEvent.setSuccess(true);
                // upload
                if (profile != null) {
                    uploadEvent.setNeedExecCommand(profile.getCommandId() != null);

                    String localFile = profile.getFile();
                    String remoteTargetDir = profile.getLocation();
                    String exclude = profile.getExclude();
                    SshStatus status = sshService.upload(project, server, localFile, remoteTargetDir, exclude);
                    if (!status.isSuccess()) {
                        uploadEvent.setSuccess(false);
                        uploadEvent.setUploadResult(status.getMessage());
                        indicator.setFraction(1f);
                        return;
                    }
                    if (profile.getCommandId() == null) {
                        indicator.setFraction(1f);
                        return;
                    }
                    indicator.setFraction(0.5f);
                }

                setTitle(String.format("Executing command on %s:%s", server.getIp(), server.getPort()));

                try {
                    // it is not recommended to executing command like "tail -f",
                    // because it will block the thread
                    SshStatus status = sshService.execute(server, cmd.generateCmdLine());
                    commandEvent.setSuccess(status.isSuccess());
                    commandEvent.setExecResult(status.getMessage());
                } catch (Exception e1) {
                    commandEvent.setSuccess(false);
                    commandEvent.setExecResult(e1.getMessage());
                }
                indicator.setFraction(1f);
            }

            @Override
            public void onFinished() {
                if (!uploadEvent.isSuccess()) {
                    Messages.showErrorDialog(uploadEvent.getUploadResult(), "Upload");
                    return;
                }
                if (!uploadEvent.getNeedExecCommand()) {
                    return;
                }
                if (!commandEvent.isSuccess()) {
                    Messages.showErrorDialog(commandEvent.getExecResult(), "Command");
                    return;
                }
                ApplicationContext.getApplicationContext().publishEvent(commandEvent);
            }
        });
    }
}