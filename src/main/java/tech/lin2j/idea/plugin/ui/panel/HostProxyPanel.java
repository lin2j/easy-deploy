package tech.lin2j.idea.plugin.ui.panel;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.util.ui.FormBuilder;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HostProxyPanel {

    private final JPanel root;
    private final SshServer contentProvider;
    private final Project project;

    private JBRadioButton noProxyRadio;
    private JBRadioButton fromSettingsRadio;

    private ComboBox<SshServer> serverComboBox;

    public HostProxyPanel(Project project, SshServer contentProvider) {
        this.project = project;
        this.contentProvider = contentProvider;

        initRadio();
        initComboBox();
        setContent();

        root = FormBuilder.createFormBuilder()
                .addComponent(noProxyRadio)
                .addComponent(fromSettingsRadio)
                .addLabeledComponent("    ",serverComboBox)
                .getPanel();
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

        noProxyRadio.addActionListener(e -> serverComboBox.setEnabled(false));
        fromSettingsRadio.addActionListener(e ->serverComboBox.setEnabled(true));
    }

    private void initComboBox() {
        List<SshServer> sshServers = ConfigHelper.sshServers();
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
        });
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
