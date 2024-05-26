package tech.lin2j.idea.plugin.ui.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.PluginSetting;
import tech.lin2j.idea.plugin.uitl.PluginUtil;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * @author linjinjia
 * @date 2024/5/25 09:45
 */
public class GeneralConfigurable implements SearchableConfigurable, Configurable.NoScroll {

    private final PluginSetting setting = ConfigHelper.pluginSetting();

    private JBCheckBox updateCheck;

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

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        setting.setUpdateCheck(updateCheck.isSelected());
    }
}