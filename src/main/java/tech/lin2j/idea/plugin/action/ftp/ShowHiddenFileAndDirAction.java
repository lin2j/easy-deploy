package tech.lin2j.idea.plugin.action.ftp;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ui.ftp.container.FileTableContainer;

/**
 * @author linjinjia
 * @date 2024/4/4 17:14
 */
public class ShowHiddenFileAndDirAction extends ToggleAction {
    private final FileTableContainer container;

    public ShowHiddenFileAndDirAction(FileTableContainer container) {
        super(
                "Show Hidden Files And Directories",
                "Show hidden files and directories",
                AllIcons.Actions.ShowHiddens
        );
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