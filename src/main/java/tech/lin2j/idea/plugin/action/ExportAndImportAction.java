package tech.lin2j.idea.plugin.action;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import icons.MyIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

/**
 * @author linjinjia
 * @date 2024/7/20 22:56
 */
public class ExportAndImportAction extends ActionGroup {

    private static final String text = MessagesBundle.getText("action.dashboard.export-import.text");


    public ExportAndImportAction() {
        super(text, text, MyIcons.Actions.ExportAndImport);
        setPopup(true);
    }

    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent e) {
        return new AnAction[]{
                new ConfigImportAction(),
                new ConfigExportAction(),
                new CleanConfigAction(),
        };
    }
}