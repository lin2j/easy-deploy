package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableGroup;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ui.settings.GeneralConfigurable;
import tech.lin2j.idea.plugin.ui.settings.SFTPConfigurable;
import tech.lin2j.idea.plugin.ui.settings.ServerTagConfigurable;

import javax.swing.SwingUtilities;
import java.util.Collections;

/**
 * @author linjinjia
 * @date 2024/4/21 18:42
 */
public class PluginSettingsDialog {
    public static Configurable[] createNewConfigurable(Project project) {
        return new Configurable[]{
//                new GeneralConfigurable(),
                new ServerTagConfigurable(),
                new SFTPConfigurable()
        };
    }

    public static void show(Project project) {
        show(project, createNewConfigurable(project), 0);
    }

    public static void show(Project project, Configurable[] configurable, int toSelect) {
        CoolConfigurableGroup coolConfigurableGroup = new CoolConfigurableGroup(configurable);
        SwingUtilities.invokeLater(() -> {
            ShowSettingsUtilImpl.getDialog(
                    project,
                    Collections.singletonList(coolConfigurableGroup),
                    configurable[toSelect]
            ).show();
        });
    }


    static class CoolConfigurableGroup implements ConfigurableGroup {
        private final Configurable[] configurables;

        public CoolConfigurableGroup(Configurable[] configurables) {
            this.configurables = configurables;
        }

        @Override
        public String getDisplayName() {
            return "Easy Deploy";
        }

        @Override
        public Configurable @NotNull [] getConfigurables() {
            return configurables;
        }
    }
}