package tech.lin2j.idea.plugin.ui.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.border.IdeaTitledBorder;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.PluginSetting;
import tech.lin2j.idea.plugin.enums.I18nType;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author linjinjia
 * @date 2024/5/25 09:45
 */
public class GeneralConfigurable implements SearchableConfigurable, Configurable.NoScroll {

    private final PluginSetting setting = ConfigHelper.pluginSetting();

    private ComboBox<I18nType> languageTypes;
    private JBCheckBox sshKeepalive;
    private JSpinner heartbeatInterval;

    @Override
    public @NotNull
    @NonNls String getId() {
        return "ED-General";
    }

    @Override
    public String getDisplayName() {
        return MessagesBundle.getText("setting.item.general");
    }

    @Override
    public @Nullable JComponent createComponent() {
        JPanel panel = FormBuilder.createFormBuilder()
                .addComponent(basic())
                .addComponent(sshControl())
                .getPanel();

        JPanel result = new JPanel(new BorderLayout());
        result.add(panel, BorderLayout.NORTH);

        return result;
    }

    @Override
    public boolean isModified() {
        I18nType selectedI18Type = languageTypes.getItemAt(languageTypes.getSelectedIndex());
        return !Objects.equals(sshKeepalive.isSelected(), setting.isSshKeepalive())
                || !Objects.equals(heartbeatInterval.getValue(), setting.getHeartbeatInterval())
                || !Objects.equals(selectedI18Type.getType(), setting.getI18nType());

    }

    @Override
    public void apply() {
        I18nType selectedI18Type = languageTypes.getItemAt(languageTypes.getSelectedIndex());
        setting.setSshKeepalive(sshKeepalive.isSelected());
        setting.setHeartbeatInterval((int) heartbeatInterval.getValue());
        setting.setI18nType(selectedI18Type.getType());
    }

    private JPanel basic() {
        String title = MessagesBundle.getText("setting.general.basic.title");
        String language = MessagesBundle.getText("setting.general.basic.language");

        languageTypes = new ComboBox<>();
        languageTypes.setModel(new CollectionComboBoxModel<>(Arrays.asList(I18nType.values())));
        languageTypes.setSelectedItem(I18nType.getByType(setting.getI18nType()));

        JPanel panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(language, languageTypes)
                .getPanel();
        panel.setBorder(new IdeaTitledBorder(title, 0, JBUI.emptyInsets()));

        return panel;
    }

    private JPanel sshControl() {
        String title = MessagesBundle.getText("setting.general.ssh.title");
        String keepaliveText = MessagesBundle.getText("setting.general.ssh.keepalive");
        String heartbeatText = MessagesBundle.getText("setting.general.ssh.keepalive.heart-beat");

        sshKeepalive = new JBCheckBox();
        sshKeepalive.setSelected(setting.isSshKeepalive());
        sshKeepalive.addChangeListener(e -> heartbeatInterval.setEnabled(sshKeepalive.isSelected()));

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
}