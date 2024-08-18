package tech.lin2j.idea.plugin.action;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import icons.MyIcons;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.ConfigImportExport;
import tech.lin2j.idea.plugin.model.event.TableRefreshEvent;
import tech.lin2j.idea.plugin.ui.dialog.ImportConfigPreviewDialog;
import tech.lin2j.idea.plugin.uitl.EncryptionUtil;
import tech.lin2j.idea.plugin.uitl.FileUtil;
import tech.lin2j.idea.plugin.uitl.ImportExportUtil;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.SwingUtilities;

/**
 * @author linjinjia
 * @date 2024/7/17 21:28
 */
public class ConfigImportAction extends NewUpdateThreadAction {
    private static final Logger log = Logger.getInstance(ConfigImportAction.class);

    private static final String text = MessagesBundle.getText("action.dashboard.export-import.import.text");

    public ConfigImportAction() {
        super(text, text, MyIcons.Actions.Import);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String filepath = ConfigHelper.pluginSetting().getDefaultExportImportPath();
        FileChooserDescriptor descriptor = allButNoMultipleChoose();
        VirtualFile virtualFile = FileChooser.chooseFile(descriptor, null, FileUtil.virtualFile(filepath));
        if (virtualFile == null) {
            return;
        }

        // request password
        String title = MessagesBundle.getText("dialog.ie.password.title");
        String tip = MessagesBundle.getText("dialog.ie.password.import.text");
        String password = Messages.showPasswordDialog(tip, title);
        if (StringUtils.isBlank(password)) {
            SwingUtilities.invokeLater(() -> Messages.showWarningDialog("Password is blank", title));
            return;
        }

        try {
            String content = EncryptionUtil.decryptFileContent(virtualFile.getPath(), password);
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
            StringBuilder err = new StringBuilder("Import failed");
            if (ex.getMessage().contains("bad key")) {
                err.append(": please check your password");
            }
            SwingUtilities.invokeLater(() -> Messages.showErrorDialog(err.toString(), "Import Error"));
        }
    }

    private FileChooserDescriptor allButNoMultipleChoose() {
        return new FileChooserDescriptor(true, false, false, false, false, false);
    }
}