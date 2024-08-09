package tech.lin2j.idea.plugin.model;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 *
 * @author linjinjia
 * @date 2024/8/9 21:40
 */
public class PluginConfigInitializer implements AppLifecycleListener {
    private static final Logger log = Logger.getInstance(PluginConfigInitializer.class);

    @Override
    public void appFrameCreated(@NotNull List<String> commandLineArgs) {
        try {
            ConfigHelper.ensureConfigLoadInMemory();
        } catch (Exception e) {
            log.error("Easy-Deploy plugin load configuration failed: " + e.getMessage(), e);
        }
    }

}
