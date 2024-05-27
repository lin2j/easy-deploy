package tech.lin2j.idea.plugin.ui.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.ui.border.IdeaTitledBorder;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.PluginSetting;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;
import tech.lin2j.idea.plugin.uitl.PluginUtil;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.util.Objects;

/**
 * @author linjinjia
 * @date 2024/5/25 09:45
 */
public class GeneralConfigurable implements SearchableConfigurable, Configurable.NoScroll {

    private final PluginSetting setting = ConfigHelper.pluginSetting();

    private JBCheckBox updateCheck;
    private JBCheckBox sshKeepalive;
    private JSpinner heartbeatInterval;

    @Override
    public @NotNull
    @NonNls String getId() {
        return "ED-General";
    }

    @Override
    public String getDisplayName() {
        return "General";
    }

    @Override
    public @Nullable JComponent createComponent() {
        JPanel panel = FormBuilder.createFormBuilder()
                .addComponent(sshControl())
                .getPanel();

        JPanel result = new JPanel(new BorderLayout());
        result.add(panel, BorderLayout.NORTH);

        return result;
    }

    @Override
    public boolean isModified() {
        return !Objects.equals(sshKeepalive.isSelected(), setting.isSshKeepalive())
                || !Objects.equals(heartbeatInterval.getValue(), setting.getHeartbeatInterval());
    }

    @Override
    public void apply() throws ConfigurationException {
//        setting.setUpdateCheck(updateCheck.isSelected());
        setting.setSshKeepalive(sshKeepalive.isSelected());
        setting.setHeartbeatInterval((int) heartbeatInterval.getValue());
    }

    private JPanel sshControl() {
        String title = MessagesBundle.getText("setting.general.ssh.title");
        String keepaliveText = MessagesBundle.getText("setting.general.ssh.keepalive");
        String heartbeatText = MessagesBundle.getText("setting.general.ssh.keepalive.heart-beat");

        sshKeepalive = new JBCheckBox();
        sshKeepalive.setSelected(setting.isSshKeepalive());
        sshKeepalive.addChangeListener(e -> {
            heartbeatInterval.setEnabled(sshKeepalive.isSelected());
        });

        int interval = setting.getHeartbeatInterval();
        heartbeatInterval = new JSpinner(new SpinnerNumberModel(interval, 10, 3600, 10));
        heartbeatInterval.setEnabled(setting.isSshKeepalive());

        JPanel panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(keepaliveText, sshKeepalive)
                .addLabeledComponent(heartbeatText, heartbeatInterval)
                .getPanel();
        panel.setBorder(new IdeaTitledBorder(title, 0, JBUI.emptyInsets()));

        return panel;
    }

    private JPanel pluginInfo() {
        updateCheck = new JBCheckBox("Update check");
        updateCheck.setSelected(setting.isUpdateCheck());
        JPanel versionPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(PluginUtil.versionLabel(), updateCheck)
                .getPanel();

        JPanel iconPanel = new JPanel(new BorderLayout());
        iconPanel.add(PluginUtil.pluginIconLabel(), BorderLayout.CENTER);

        JPanel panel = FormBuilder.createFormBuilder()
                .addComponent(iconPanel)
                .addComponent(versionPanel)
                .getPanel();

        JPanel result = new JPanel(new BorderLayout());
        result.add(panel, BorderLayout.NORTH);

        return result;
    }
}