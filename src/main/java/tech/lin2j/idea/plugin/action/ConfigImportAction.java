package tech.lin2j.idea.plugin.action;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.model.ConfigImportExport;
import tech.lin2j.idea.plugin.model.event.TableRefreshEvent;
import tech.lin2j.idea.plugin.uitl.ImportExportUtil;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author linjinjia
 * @date 2024/7/17 21:28
 */
public class ConfigImportAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        FileChooserDescriptor descriptor = allButNoMultipleChoose();
        VirtualFile virtualFile = FileChooser.chooseFile(descriptor, null, null);
        if (virtualFile == null) {
            return;
        }
        try {
            ConfigImportExport dto = new Gson().fromJson(new InputStreamReader(virtualFile.getInputStream()), ConfigImportExport.class);
            ImportExportUtil.importConfig(dto);
            ApplicationContext.getApplicationContext().publishEvent(new TableRefreshEvent());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private FileChooserDescriptor allButNoMultipleChoose() {
        return new FileChooserDescriptor(true, false, false, false, false, false);
    }
}