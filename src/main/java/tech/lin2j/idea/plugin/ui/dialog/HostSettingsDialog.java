package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.action.TestConnectionAction;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.event.TableRefreshEvent;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ui.panel.HostBasicPanel;
import tech.lin2j.idea.plugin.ui.panel.HostProxyPanel;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class HostSettingsDialog extends DialogWrapper {
    private final Project project;
    private final JPanel root = new JPanel(new BorderLayout());
    private final HostBasicPanel hostBasicPanel;
    private final HostProxyPanel hostProxyPanel;

    private SshServer server;

    public HostSettingsDialog(Project project, SshServer server) {
        super(project);
        this.project = project;
        this.server = server;

        JButton testButton = new JButton(MessagesBundle.getText("dialog.panel.host.test-connect"));
        testButton.addActionListener(this::testConnect);

        hostBasicPanel = new HostBasicPanel(project, server, testButton);
        hostProxyPanel = new HostProxyPanel(project, server);

        setTitle(MessagesBundle.getText("dialog.host.title"));
        setSize(500, 0);
        init();
    }

    @Override
    protected void doOKAction() {
        boolean isAdd = false;
        if (server == null) {
            server = new SshServer();
            isAdd = true;
        }
        boolean isOk = hostBasicPanel.saveServerInfo(server, false);
        if (!isOk) {
            return;
        }
        server.setProxy(hostProxyPanel.getProxy());

        if (isAdd) {
            ConfigHelper.addSshServer(server);
        }

        ApplicationContext.getApplicationContext().publishEvent(new TableRefreshEvent());

        super.doOKAction();
    }

    private void testConnect(ActionEvent e) {
        SshServer test = server;
        if (test == null) {
            test = new SshServer();
        }
        boolean isOk = hostBasicPanel.saveServerInfo(test, true);
        if (!isOk) {
            return;
        }
        test.setProxy(hostProxyPanel.getProxy());

        new TestConnectionAction(project, test).actionPerformed(e);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        String basicTab = MessagesBundle.getText("dialog.host.tab.basic");
        String proxyTab = MessagesBundle.getText("dialog.host.tab.proxy");

        JBTabsImpl tabs = new JBTabsImpl(project);
        tabs.addTab(new TabInfo(hostBasicPanel.createUI()).setText(basicTab));
        tabs.addTab(new TabInfo(hostProxyPanel.createUI()).setText(proxyTab));
        root.add(tabs.getComponent(), BorderLayout.CENTER);

        return tabs;
    }
}
