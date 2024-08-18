package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.action.CopyCommandAction;
import tech.lin2j.idea.plugin.enums.AuthType;
import tech.lin2j.idea.plugin.event.ApplicationListener;
import tech.lin2j.idea.plugin.model.Command;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.NoneCommand;
import tech.lin2j.idea.plugin.model.SeparatorCommand;
import tech.lin2j.idea.plugin.model.event.CommandAddEvent;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ui.render.CommandColoredListCellRenderer;
import tech.lin2j.idea.plugin.uitl.CommandUtil;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;
import tech.lin2j.idea.plugin.uitl.UiUtil;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
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
    private Command selectedCommand;

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
        commandList.setCellRenderer(new CommandColoredListCellRenderer(sshId));
        commandList.addListSelectionListener(e -> {
            Command command = commandList.getSelectedValue();
            if (command != null) {
                showInput.setText(command.toString());
                selectedCommand = command;
            }
        });

        new DoubleClickListener() {
            @Override
            protected boolean onDoubleClick(MouseEvent mouseEvent) {
                runCommand();
                return true;
            }
        }.installOn(commandList);
    }

    private JPanel createCommandToolbarPanel() {
        return ToolbarDecorator.createDecorator(commandList)
                .setToolbarPosition(ActionToolbarPosition.TOP)
                .disableUpDownActions()
                .setEditActionUpdater(e -> isEditable())
                .setRemoveActionUpdater(e -> isEditable())
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
                .addExtraAction(AnActionButton.fromAction(new CopyCommandAction(() -> selectedCommand)))
                .addExtraAction(new RunCommandAction())
                .createPanel();
    }

    private boolean isEditable() {
        Command selectedValue = commandList.getSelectedValue();
        if (selectedValue instanceof SeparatorCommand) {
            return false;
        }
        if (selectedValue != null) {
            return !selectedValue.getSharable() || Objects.equals(selectedValue.getSshId(), sshId);
        }
        return true;
    }

    public void loadCommandList() {
        List<Command> commands = ConfigHelper.getCommandsBySshId(sshId);
        List<Command> sharableCommands = ConfigHelper.getSharableCommands(sshId);

        List<Command> data = new ArrayList<>(commands);
        data.add(new SeparatorCommand());
        data.addAll(sharableCommands);

        if (data.size() == 1) {
            data = new ArrayList<>();
        }

        commandList.setListData(data.toArray(new Command[0]));
    }

    private void runCommand() {
        Command cmd = commandList.getSelectedValue();
        if (Objects.isNull(cmd) || cmd instanceof SeparatorCommand || cmd instanceof NoneCommand) {
            return;
        }
        SshServer server = ConfigHelper.getSshServerById(sshId);

        boolean needPassword = AuthType.needPassword(server.getAuthType());
        server = UiUtil.requestPasswordIfNecessary(server);
        if (needPassword && StringUtil.isEmpty(server.getPassword())) {
            return;
        }
        CommandUtil.executeAndShowMessages(project, cmd, null, server, this);
    }

    private class RunCommandAction extends AnActionButton {

        public RunCommandAction() {
            super("Run", "Run command", AllIcons.Actions.RunAll);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            runCommand();
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.BGT;
        }
    }
}