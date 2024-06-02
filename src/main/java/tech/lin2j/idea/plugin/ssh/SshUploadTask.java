package tech.lin2j.idea.plugin.ssh;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.util.PathUtil;
import net.schmizz.sshj.xfer.TransferListener;
import tech.lin2j.idea.plugin.domain.model.Command;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.UploadProfile;
import tech.lin2j.idea.plugin.factory.SshServiceFactory;
import tech.lin2j.idea.plugin.file.ConsoleTransferListener;
import tech.lin2j.idea.plugin.file.ExtensionFilter;
import tech.lin2j.idea.plugin.file.FileFilter;
import tech.lin2j.idea.plugin.service.ISshService;

import java.io.File;

/**
 * @author linjinjia
 * @date 2024/5/2 17:11
 */
public class SshUploadTask implements Runnable{
    private static final ISshService sshService = SshServiceFactory.getSshService();

    private final int sshId;
    private final int profileId;
    private final ConsoleView console;

    public SshUploadTask(ConsoleView console, int sshId, int profileId) {
        this.console = console;
        this.sshId = sshId;
        this.profileId = profileId;
    }

    @Override
    public void run() {
        UploadProfile profile = ConfigHelper.getOneUploadProfileById(sshId, profileId);
        if (profile == null) {
            return;
        }
        SshServer server = ConfigHelper.getSshServerById(sshId);

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
        println("Command completed: {" + cmdLine + "}");
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

}