package tech.lin2j.idea.plugin.uitl;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.IconUtil;
import icons.MyIcons;

import javax.swing.Icon;

/**
 * @author linjinjia
 * @date 2024/5/26 15:14
 */
public class PluginUtil {

    private static final PluginId PLUGIN_ID = PluginId.getId("tech.lin2j.simple-deployment");
    private static final String version = PluginManagerCore.getPlugin(PLUGIN_ID).getVersion();

    public static String version() {
        return version;
    }

    public static JBLabel versionLabel() {
        return new JBLabel("Version: " + PluginUtil.version());
    }

    public static JBLabel pluginIconLabel() {
        JBLabel label = new JBLabel();
        Icon icon = IconUtil.scale(MyIcons.EasyDeploy, label,4f);
        label.setIcon(icon);
        return label;
    }


}