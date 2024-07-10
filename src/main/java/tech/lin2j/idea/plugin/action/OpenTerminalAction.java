package tech.lin2j.idea.plugin.action;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.TerminalTabState;
import org.jetbrains.plugins.terminal.TerminalView;
import org.jetbrains.plugins.terminal.cloud.CloudTerminalRunner;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.enums.AuthType;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.SshStatus;
import tech.lin2j.idea.plugin.ssh.exception.RemoteSdkException;
import tech.lin2j.idea.plugin.uitl.TerminalRunnerUtil;
import tech.lin2j.idea.plugin.uitl.UiUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author linjinjia
 * @date 2024/5/5 12:25
 */
public class OpenTerminalAction implements ActionListener {

    private final int sshId;
    private final Project project;
    private final String workingDirectory;

    public OpenTerminalAction(int sshId, Project project, @Nullable String workingDirectory) {
        this.sshId = sshId;
        this.project = project;
        this.workingDirectory = workingDirectory;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SshServer tmp = ConfigHelper.getSshServerById(sshId);

        boolean needPassword = AuthType.needPassword(tmp.getAuthType());
        SshServer server = UiUtil.requestPasswordIfNecessary(tmp);
        if (needPassword && StringUtil.isEmpty(server.getPassword())) {
            return;
        }
        SshStatus status = new SshStatus(false, null);
        String title = String.format("Opening terminal %s:%s", server.getIp(), server.getPort());
        ProgressManager.getInstance().run(new Task.Backgroundable(project, title) {
            CloudTerminalRunner runner = null;
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(false);
                try {
                    runner = TerminalRunnerUtil.createCloudTerminalRunner(project, server, workingDirectory);
                    status.setSuccess(true);
                } catch (RemoteSdkException ex) {
                    status.setMessage("Error connecting server: " + ex.getMessage());
                } finally {
                    indicator.setFraction(1);
                }
            }

            @Override
            public void onFinished() {
                if(!status.isSuccess()) {
                    Messages.showErrorDialog(status.getMessage(), "Error");
                    return ;
                }
                TerminalView terminalView = TerminalView.getInstance(project);
                TerminalTabState tabState = new TerminalTabState();
                tabState.myTabName = server.getIp() + ":" + server.getPort();
                terminalView.createNewSession(runner, tabState);
            }
        });
    }
}