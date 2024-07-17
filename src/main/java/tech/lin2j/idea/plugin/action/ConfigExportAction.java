package tech.lin2j.idea.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.model.ConfigImportExport;
import tech.lin2j.idea.plugin.uitl.FileUtil;
import tech.lin2j.idea.plugin.uitl.ImportExportUtil;
import tech.lin2j.idea.plugin.uitl.PluginUtil;

/**
 * @author linjinjia
 * @date 2024/7/17 21:19
 */
public class ConfigExportAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            ConfigImportExport dto = ImportExportUtil.exportBaseOnOptions(ImportExportUtil.allExport());
            String filepath = "/Users/kenny/Downloads/EasyDeploy@" + PluginUtil.version() + ".json";
            ImportExportUtil.exportConfigToJsonFile(dto, filepath);
            FileUtil.openDir("/Users/kenny/Downloads/");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}