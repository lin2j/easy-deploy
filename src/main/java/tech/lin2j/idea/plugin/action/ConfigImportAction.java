package tech.lin2j.idea.plugin.action;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import icons.MyIcons;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.model.ConfigImportExport;
import tech.lin2j.idea.plugin.model.event.TableRefreshEvent;
import tech.lin2j.idea.plugin.ui.dialog.ImportConfigPreviewDialog;
import tech.lin2j.idea.plugin.uitl.FileUtil;
import tech.lin2j.idea.plugin.uitl.ImportExportUtil;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author linjinjia
 * @date 2024/7/17 21:28
 */
public class ConfigImportAction extends AnAction {
    private static final Logger log = Logger.getInstance(ConfigImportAction.class);

    private static final String text = MessagesBundle.getText("action.dashboard.export-import.import.text");

    public ConfigImportAction() {
        super(text, text, MyIcons.Actions.Import);
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        FileChooserDescriptor descriptor = allButNoMultipleChoose();
        VirtualFile virtualFile = FileChooser.chooseFile(descriptor, null, FileUtil.getHome());
        if (virtualFile == null) {
            return;
        }

        try (
                InputStreamReader reader = new InputStreamReader(virtualFile.getInputStream());
                BufferedReader br = new BufferedReader(reader)
        ) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            String content = sb.toString();

            boolean isOk = new ImportConfigPreviewDialog(e.getProject(), content).showAndGet();
            if (!isOk) {
                return;
            }

            ConfigImportExport dto = new Gson().fromJson(content, ConfigImportExport.class);
            ImportExportUtil.importConfig(dto);

            TableRefreshEvent refreshEvent = new TableRefreshEvent();
            refreshEvent.setTagRefresh(dto.getOptions().isServerTags());
            ApplicationContext.getApplicationContext().publishEvent(refreshEvent);

        } catch (Exception ex) {
            log.error(ex);
            SwingUtilities.invokeLater(() -> Messages.showErrorDialog("Import failed", "Import Error"));
        }
    }

    private FileChooserDescriptor allButNoMultipleChoose() {
        return new FileChooserDescriptor(true, false, false, false, false, false);
    }
}