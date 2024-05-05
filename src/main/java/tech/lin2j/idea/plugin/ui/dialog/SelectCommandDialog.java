package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.domain.model.Command;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.event.CommandAddEvent;
import tech.lin2j.idea.plugin.enums.AuthType;
import tech.lin2j.idea.plugin.event.ApplicationListener;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ui.render.CommandColoredListCellRenderer;
import tech.lin2j.idea.plugin.uitl.CommandUtil;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;
import tech.lin2j.idea.plugin.uitl.UiUtil;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.util.List;
import java.util.Objects;

/**
 * @author linjinjia
 * @date 2024/5/4 22:38
 */
public class SelectCommandDialog extends DialogWrapper implements ApplicationListener<CommandAddEvent>  {
    private final JPanel root;
    private JBTextField showInput;
    private JBList<Command> commandList;

    private final int sshId;
    private final Project project;

    public SelectCommandDialog(Project project, int sshId) {
        super(project);
        this.sshId = sshId;
        this.project = project;

        initInput();
        initCommandList();
        loadCommandList();

        root = FormBuilder.createFormBuilder()
                .addLabeledComponent(MessagesBundle.getText("dialog.command.select.show"), showInput, true)
                .addComponentFillVertically(createCommandToolbarPanel(), 8)
                .getPanel();
        root.setPreferredSize(new Dimension(UiUtil.screenWidth() / 2, 400));

        setTitle(MessagesBundle.getText("dialog.command.select.frame"));
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return root;
    }

    @Override
    public void onApplicationEvent(CommandAddEvent event) {
        loadCommandList();
    }

    private void initInput() {
        showInput = new JBTextField();
    }

    private void initCommandList() {
        commandList = new JBList<>();
        commandList.setCellRenderer(new CommandColoredListCellRenderer());
        commandList.addListSelectionListener(e -> {
            Command command = commandList.getSelectedValue();
            if (command != null) {
                showInput.setText(command.toString());
            }
        });
    }

    private JPanel createCommandToolbarPanel() {
        return ToolbarDecorator.createDecorator(commandList)
                .setToolbarPosition(ActionToolbarPosition.TOP)
                .disableUpDownActions()
                .setAddAction(e -> {
                    Command command = new Command();
                    command.setSshId(sshId);
                    new AddCommandDialog(project, command).showAndGet();
                })
                .setRemoveAction(e -> {
                    Command cmd = commandList.getSelectedValue();
                    if (cmd == null) {
                        return;
                    }
                    boolean confirm = UiUtil.deleteConfirm(cmd.toString());
                    if (confirm) {
                        ConfigHelper.removeCommand(cmd);
                        loadCommandList();
                    }
                })
                .setEditAction(e -> {
                    Command cmd = commandList.getSelectedValue();
                    if (cmd == null) {
                        return;
                    }
                    new AddCommandDialog(project, cmd).showAndGet();
                })
                .addExtraAction(new RunCommandAction(this))
                .createPanel();
    }

    public void loadCommandList() {
        List<Command> commands = ConfigHelper.getCommandsBySshId(sshId);
        if (commands.size() == 0) {
            showInput.setText("");
            commandList.setListData(new Command[0]);
            return;
        }
        commandList.setListData(commands.toArray(new Command[0]));
    }

    private class RunCommandAction extends AnActionButton {

        private final DialogWrapper dialogWrapper;

        public RunCommandAction(DialogWrapper dialogWrapper) {
            super("Run", "Run command", AllIcons.Actions.RunAll);
            this.dialogWrapper = dialogWrapper;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Command cmd = commandList.getSelectedValue();
            if (Objects.isNull(cmd)) {
                return;
            }
            SshServer server = ConfigHelper.getSshServerById(cmd.getSshId());

            boolean needPassword = AuthType.needPassword(server.getAuthType());
            server = UiUtil.requestPasswordIfNecessary(server);
            if (needPassword && StringUtil.isEmpty(server.getPassword())) {
                return;
            }
            CommandUtil.executeAndShowMessages(project, cmd, null, server, dialogWrapper);
        }
    }
}