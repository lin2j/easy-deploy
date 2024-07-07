package tech.lin2j.idea.plugin.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import icons.MyIcons;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.domain.model.Command;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.ui.dialog.AddCommandDialog;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author linjinjia
 * @date 2024/7/7 16:07
 */
public class AddCommandAction extends AnAction {

    private static final String text = MessagesBundle.getText("action.common.command.add.text");

    private final int sshId;
    private final Project project;
    private final Consumer<Command> action;

    public AddCommandAction(Project project, int sshId, Consumer<Command> action) {
        super(text, text, MyIcons.Actions.Add);
        this.sshId = sshId;
        this.project = project;
        this.action = action;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Command tmp = new Command(sshId);
        boolean exitOk = new AddCommandDialog(project, tmp).showAndGet();
        if (exitOk && action != null) {
            List<Command> list =ConfigHelper.getCommandsBySshId(sshId);
            Command newCmd = list.get(list.size() - 1);
            action.accept(newCmd);
        }
    }
}