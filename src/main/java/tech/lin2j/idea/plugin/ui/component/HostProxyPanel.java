package tech.lin2j.idea.plugin.ui.component;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HostProxyPanel {

    private final JPanel root;
    private final Project project;
    private SshServer contentProvider;

    private JBRadioButton noProxyRadio;
    private JBRadioButton fromSettingsRadio;

    private ComboBox<SshServer> serverComboBox;

    private JPanel proxyContainer;
    private HostDependencyPanel dependencyPanel;

    public HostProxyPanel(Project project, SshServer contentProvider) {
        this.project = project;
        this.contentProvider = contentProvider;
        if (contentProvider != null) {
            // fix bug: proxy is still saved after cancellation
            this.contentProvider = contentProvider.clone();
        }

        initRadio();
        initComboBox();
        initProxyDependencyContainer();
        setContent();

        root = new JPanel(new GridBagLayout());
        root.add(noProxyRadio, new GridBagConstraints(0, 0, 1, 1, 1, 0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, JBUI.insetsTop(5), 0, 0));
        root.add(proxyContainer, new GridBagConstraints(0, 1, 1, 1, 1, 0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, JBUI.emptyInsets(), 0, 0));
    }

    public JPanel createUI() {
        JPanel ui = new JPanel(new BorderLayout());
        ui.add(root, BorderLayout.NORTH);
        return ui;
    }

    public Integer getProxy() {
        if (noProxyRadio.isSelected()) {
            return null;
        }
        Object obj = serverComboBox.getSelectedItem();
        if (obj != null) {
            return ((SshServer) obj).getId();
        }
        return null;
    }

    private void initRadio() {
        noProxyRadio = new JBRadioButton(MessagesBundle.getText("dialog.panel.host.proxy.radio.no-proxy"));
        fromSettingsRadio = new JBRadioButton(MessagesBundle.getText("dialog.panel.host.proxy.radio.from-settings"));

        ButtonGroup group = new ButtonGroup();
        group.add(noProxyRadio);
        group.add(fromSettingsRadio);

        noProxyRadio.addActionListener(e -> {
            serverComboBox.setEnabled(false);
            dependencyPanel.setVisible(false);
        });
        fromSettingsRadio.addActionListener(e -> {
            serverComboBox.setEnabled(true);
            dependencyPanel.setVisible(true);
        });
    }

    private void initComboBox() {
        List<SshServer> sshServers = new ArrayList<>(ConfigHelper.sshServers());
        sshServers.add(0, SshServer.None);
        if (contentProvider != null && StringUtil.isNotEmpty(contentProvider.getIp())) {
            sshServers = sshServers.stream()
                    .filter(s -> !Objects.equals(s.getIp(), contentProvider.getIp()))
                    .collect(Collectors.toList());
        }

        serverComboBox = new ComboBox<>();
        serverComboBox.setModel(new CollectionComboBoxModel<>(sshServers));
        serverComboBox.addItemListener(e -> {
            SshServer proxy = (SshServer) serverComboBox.getSelectedItem();
            if (proxy != null && contentProvider != null) {
                contentProvider.setProxy(proxy.getId());
            }
            refreshProxyChainPanel();
        });
    }

    private void initProxyDependencyContainer() {
        dependencyPanel = new HostDependencyPanel();
        JBScrollPane scrollPane = new JBScrollPane(dependencyPanel);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        scrollPane.setBorder(IdeBorderFactory.createEmptyBorder(JBUI.emptyInsets()));

        proxyContainer = new JPanel(new GridBagLayout());
        proxyContainer.add(fromSettingsRadio, new GridBagConstraints(0, 0, 1, 1, 1, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, JBUI.emptyInsets(), 0, 0));
        proxyContainer.add(serverComboBox, new GridBagConstraints(0, 1, 1, 1, 1, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, JBUI.insets(0, 20, 0, 0), 0, 0));
        proxyContainer.add(scrollPane, new GridBagConstraints(0, 2, 1, 1, 1, 1,
                GridBagConstraints.WEST, GridBagConstraints.BOTH, JBUI.insets(10, 20, 0, 0), 0, 0));

        refreshProxyChainPanel();
    }

    private void refreshProxyChainPanel() {
        SshServer server = contentProvider;
        if (server == null) {
            server = new SshServer("This");
            server.setProxy(getProxy());
        }
        List<HostChainItem> hostChain = new ArrayList<>();
        hostChain.add(new HostChainItem(server, false));

        boolean hasCycle = false;
        while (server.getProxy() != null) {
            server = ConfigHelper.getSshServerById(server.getProxy());
            if (server == null) {
                break;
            }

            // check cycle
            SshServer finalServer = server;
            boolean isContains = hostChain.stream()
                    .anyMatch(item -> {
                        boolean equals = Objects.equals(item.getServer(), finalServer);
                        if (equals) {
                            item.setCycle(true);
                        }
                        return equals;
                    });
            if (isContains) {
                hasCycle = true;
                hostChain.add(new HostChainItem(server, true));
                break;
            }

            hostChain.add(new HostChainItem(server, false));
        }
        dependencyPanel.setHostList(hostChain, hasCycle);
    }

    private void setContent() {
        if (contentProvider != null && contentProvider.getProxy() != null) {
            fromSettingsRadio.setSelected(true);
            fromSettingsRadio.doClick();

            SshServer proxy = ConfigHelper.getSshServerById(contentProvider.getProxy());
            serverComboBox.setSelectedItem(proxy);
        } else {
            noProxyRadio.setSelected(true);
            noProxyRadio.doClick();
        }
    }
}
