package tech.lin2j.idea.plugin.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import icons.MyIcons;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.event.TableRefreshEvent;
import tech.lin2j.idea.plugin.ui.dialog.InputConfirmDialog;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

/**
 *
 * @author linjinjia
 * @date 2024/7/28 18:21
 */
public class CleanConfigAction extends NewUpdateThreadAction {

    private static final String text = MessagesBundle.getText("action.dashboard.clean.text");

    public CleanConfigAction() {
        super(text, text, MyIcons.Actions.Clean);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String title = MessagesBundle.getText("dialog.clean.title");
        String msg = MessagesBundle.getText("dialog.clean.tip");
        String okText = MessagesBundle.getText("dialog.clean.ok-text");
        String confirm = "Clean All Configuration";

        boolean isOk = new InputConfirmDialog(null, title, msg, confirm, okText).showAndGet();
        if (isOk) {
            ConfigHelper.cleanConfig();
            ApplicationContext.getApplicationContext().publishEvent(new TableRefreshEvent(true));
        }
    }
}
