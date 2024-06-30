package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import icons.MyIcons;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ui.ftp.container.FileTableContainer;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

/**
 * @author linjinjia
 * @date 2024/4/4 17:14
 */
public class ShowHiddenFileAndDirAction extends ToggleAction {
    private static final String text = MessagesBundle.getText("action.ftp.hidden.text");
    private static final String desc = MessagesBundle.getText("action.ftp.hidden.description");

    private final FileTableContainer container;

    public ShowHiddenFileAndDirAction(FileTableContainer container) {
        super(text, desc, MyIcons.Actions.showHidden);
        this.container = container;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return !container.showHiddenFileAndDir();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        container.reversedHiddenFlag();
        container.refreshFileList();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
    }
}