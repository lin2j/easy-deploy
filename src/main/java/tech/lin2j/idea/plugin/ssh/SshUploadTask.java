package tech.lin2j.idea.plugin.ssh;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import net.schmizz.sshj.xfer.TransferListener;
import tech.lin2j.idea.plugin.domain.model.Command;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.UploadProfile;
import tech.lin2j.idea.plugin.factory.SshServiceFactory;
import tech.lin2j.idea.plugin.file.ConsoleFileFilterAdapter;
import tech.lin2j.idea.plugin.file.ConsoleTransferListener;
import tech.lin2j.idea.plugin.file.ExtensionFilter;
import tech.lin2j.idea.plugin.file.FileFilter;
import tech.lin2j.idea.plugin.service.ISshService;

/**
 * @author linjinjia
 * @date 2024/5/2 17:11
 */
public class SshUploadTask implements Runnable{
    private static final ISshService sshService = SshServiceFactory.getSshService();

    private final int sshId;
    private final int profileId;
    private final Project project;
    private final ConsoleView console;

    public SshUploadTask(Project project, ConsoleView console, int sshId, int profileId) {
        this.project = project;
        this.console = console;
        this.sshId = sshId;
        this.profileId = profileId;
    }

    @Override
    public void run() {
        UploadProfile profile = ConfigHelper.getOneUploadProfileByName(sshId, profileId);
        if (profile == null) {
            return;
        }
        SshServer server = ConfigHelper.getSshServerById(sshId);

        String localFile = profile.getFile();
        String remoteTargetDir = profile.getLocation();
        String exclude = profile.getExclude();

        log("file upload success: " + localFile + "\n");
        String initMsg = "upload [" + localFile + "] to [" + remoteTargetDir + "]\n";
        FileFilter filter = new ConsoleFileFilterAdapter(console, new ExtensionFilter(exclude), server, initMsg);
        TransferListener transferListener = new ConsoleTransferListener(localFile, console);
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
        SshStatus execStatus = sshService.execute(server, cmdLine);
        if (!execStatus.isSuccess()) {
            error("execute command failed: {" + cmdLine + "}: " + execStatus.getMessage() + "\n");
            return;
        }
        log("command completed: {" + cmdLine + "}\n");
        log(execStatus.getMessage() + "\n");

    }

    private void log(String text) {
        console.print(text, ConsoleViewContentType.NORMAL_OUTPUT);
    }

    private void error(String err) {
        console.print(err, ConsoleViewContentType.ERROR_OUTPUT);
    }

}