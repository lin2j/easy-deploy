package tech.lin2j.idea.plugin.action;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.service.ISshService;
import tech.lin2j.idea.plugin.service.impl.PluginNotificationService;
import tech.lin2j.idea.plugin.service.impl.SshjSshService;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.SshStatus;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author linjinjia
 * @date 2024/11/28 22:27
 */
public class TestConnectionAction implements ActionListener {
    private final SshServer sshServer;
    private final Project project;
    private final ISshService sshService;
    private final PluginNotificationService notificationService;

    public TestConnectionAction(Project project, SshServer server) {
        this.sshServer = server;
        this.project = project;
        sshService = ApplicationManager.getApplication().getService(ISshService.class);
        notificationService = ApplicationManager.getApplication().getService(PluginNotificationService.class);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String testTitle = MessagesBundle.getText("dialog.panel.host.test-connect.testing");
        ProgressManager.getInstance().run(new Task.Backgroundable(project, testTitle) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                java.util.List<String> ipList = sshServer.getIpList();
                if (ipList.isEmpty()) {
                    notificationService.showNotification(project,
                        MessagesBundle.getText("dialog.panel.host.test-connect.title"),
                        MessagesBundle.getText("dialog.panel.host.test-connect.no-ip"));
                    return;
                }

                SshStatus lastStatus = null;
                String currentIp = "";
                String password = sshServer.getPassword();
                String passPhrase = sshServer.getPassPhrase();
                for (String ip : ipList) {
                    currentIp = ip.trim();
                    if (currentIp.isEmpty()) continue;

                    indicator.setText(String.format("%s %s:%s", testTitle, currentIp, sshServer.getPort()));

                    SshServer testServer = sshServer.clone();
                    testServer.setIp(currentIp);
                    testServer.setPassword(password);
                    testServer.setPassPhrase(passPhrase);
                    lastStatus = sshService.isValid(testServer);

                    if (!lastStatus.isSuccess()) {
                        break;
                    }
                }

                String title = MessagesBundle.getText("dialog.panel.host.test-connect.title");
                String tip = MessagesBundle.getText("dialog.panel.host.test-connect.tip");
                String msg;
                if (lastStatus == null) {
                    msg = MessagesBundle.getText("dialog.panel.host.test-connect.no-ip");
                } else if (lastStatus.isSuccess()) {
                    msg = tip;
                } else {
                    msg = MessagesBundle.getText("dialog.panel.host.test-connect.failed", currentIp, lastStatus.getMessage());
                }
                notificationService.showNotification(project, title, msg);
            }
        });
    }
}
