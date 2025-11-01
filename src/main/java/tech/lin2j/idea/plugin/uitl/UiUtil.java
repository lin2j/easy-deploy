package tech.lin2j.idea.plugin.uitl;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.table.JBTable;
import tech.lin2j.idea.plugin.enums.AuthType;
import tech.lin2j.idea.plugin.ssh.SshServer;

import java.awt.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author linjinjia
 * @date 2022/7/1 22:08
 */
public class UiUtil {
    public static CompletableFuture<String> getUserInputAsync() {
        CompletableFuture<String> future = new CompletableFuture<>();
        String tip = MessagesBundle.getText("dialog.password.tip");
        String title = MessagesBundle.getText("dialog.password.frame");
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                Project project = CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext());
                String password = Messages.showPasswordDialog(project, tip, title, Messages.getQuestionIcon());
                if (StringUtil.isNotEmpty(password)) {
                    future.complete(password);
                } else {
                    Messages.showErrorDialog(project, MessagesBundle.getText("dialog.password.error"), "Error");
                    future.cancel(true);
                }
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * pop up an option pane when clicking the delete button
     *
     * @param specific specific message to show after the default message.
     *                 if it is empty, only the default message will be
     *                 displayed.
     * @return true if user confirms to do this action, or return false
     */
    public static boolean deleteConfirm(String specific) {
        String defaultMessage = "Are you sure you want to remove the selected item?";
        if (StringUtil.isNotEmpty(specific)) {
            defaultMessage = defaultMessage + "\n" + specific;
        }
        MessageDialogBuilder.YesNo warning = MessageDialogBuilder.yesNo("Warning", defaultMessage);
        return warning.ask((Project) null);
    }

    public static int screenWidth() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        return toolkit.getScreenSize().width;
    }

    public static void hideTableLine(JBTable table) {
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
    }
}
