package tech.lin2j.idea.plugin.action;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.factory.SshServiceFactory;
import tech.lin2j.idea.plugin.service.ISshService;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.SshStatus;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TestConnectionAction implements ActionListener {

    private final ISshService sshService = SshServiceFactory.getSshService();


    private final SshServer sshServer;
    private final Project project;

    public TestConnectionAction(Project project, SshServer server) {
        this.sshServer = server;
        this.project = project;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String title = String.format("Testing %s:%s", sshServer.getIp(), sshServer.getPort());
        ProgressManager.getInstance().run(new Task.Backgroundable(project, title) {
            SshStatus status = null;

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(false);
                status = sshService.isValid(sshServer);
                indicator.setFraction(1.0);
            }

            @Override
            public void onFinished() {
                String title = MessagesBundle.getText("dialog.panel.host.test-connect.title");
                if (status.isSuccess()) {
                    String tip = MessagesBundle.getText("dialog.panel.host.test-connect.tip");
                    Messages.showMessageDialog(tip, title, Messages.getInformationIcon());
                } else {
                    Messages.showErrorDialog(status.getMessage(), title);
                }
            }
        });
    }
}
