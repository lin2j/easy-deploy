package tech.lin2j.idea.plugin.ssh;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.util.PathUtil;
import net.schmizz.sshj.xfer.TransferListener;
import tech.lin2j.idea.plugin.factory.SshServiceFactory;
import tech.lin2j.idea.plugin.file.ConsoleTransferListener;
import tech.lin2j.idea.plugin.file.ExtensionFilter;
import tech.lin2j.idea.plugin.file.FileFilter;
import tech.lin2j.idea.plugin.model.Command;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.DeployProfile;
import tech.lin2j.idea.plugin.model.UploadProfile;
import tech.lin2j.idea.plugin.service.ISshService;

import java.io.File;
import java.util.Objects;

/**
 * @author linjinjia
 * @date 2024/5/2 17:11
 */
public class SshUploadTask implements Runnable{
    private static final ISshService sshService = SshServiceFactory.getSshService();

    private final int sshId;
    private final int profileId;
    private final ConsoleView console;

    private UploadProfile profile;
    private SshServer server;
    private String taskName;

    public SshUploadTask(ConsoleView console, DeployProfile deployProfile) {
        this.sshId = deployProfile.getSshId();
        this.profileId = deployProfile.getProfileId();
        this.console = console;
        this.server = getServer();
        this.profile = getProfile();
        Objects.requireNonNull(server);
        Objects.requireNonNull(profile);

        this.taskName = String.format("%s - %s", server.getIp(), profile.getName());
    }

    public SshUploadTask(ConsoleView console, int sshId, int profileId) {
        this.console = console;
        this.sshId = sshId;
        this.profileId = profileId;
    }

    @Override
    public void run() {
        UploadProfile profile = getProfile();
        if (profile == null) {
            return;
        }
        SshServer server = getServer();

        String localFile = profile.getFile();
        String remoteTargetDir = profile.getLocation();
        String exclude = profile.getExclude();

        String realPath = localFile;
        if (new File(localFile).isFile()) {
            realPath = PathUtil.getParentPath(localFile);
        }
        TransferListener transferListener = new ConsoleTransferListener(realPath, console);

        FileFilter filter = new ExtensionFilter(exclude);
        SshStatus uploadStatus = sshService.upload(filter, server, localFile, remoteTargetDir, transferListener);

        if (!uploadStatus.isSuccess()) {
            error(uploadStatus.getMessage());
            return;
        }
        Integer cmdId = profile.getCommandId();
        if (cmdId == null) {
            return;
        }
        Command cmd = ConfigHelper.getCommandById(cmdId);
        if (cmd == null) {
            return;
        }

        String cmdLine = cmd.generateCmdLine();
        println("Execute custom command: " + cmdLine);
        SshStatus execStatus = sshService.execute(server, cmdLine);
        if (!execStatus.isSuccess()) {
            error("Execute command failed: " + execStatus.getMessage() + "\n");
            return;
        }
        println("Command completed:");
        println(cmd.logString());
        println(execStatus.getMessage());
    }

    private void println(String text) {
        console.print("[INFO] ", ConsoleViewContentType.LOG_INFO_OUTPUT);
        console.print(text + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
    }

    private void error(String err) {
        console.print("[ERROR] ", ConsoleViewContentType.LOG_ERROR_OUTPUT);
        console.print(err + "\n", ConsoleViewContentType.ERROR_OUTPUT);
    }

    public UploadProfile getProfile() {
        if (profile == null) {
            profile = ConfigHelper.getOneUploadProfileById(sshId, profileId);
        }
        return profile;
    }

    public SshServer getServer() {
        if (server == null) {
            server = ConfigHelper.getSshServerById(sshId);
        }
        return server;
    }

    public String getTaskName() {
        return taskName;
    }
}