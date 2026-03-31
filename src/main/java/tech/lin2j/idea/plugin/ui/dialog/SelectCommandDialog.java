package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.util.text.Strings;
import com.intellij.ui.DocumentAdapter;
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

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * @author linjinjia
 * @date 2024/5/4 22:38
 */
public class SelectCommandDialog extends DialogWrapper implements ApplicationListener<CommandAddEvent> {
    private static final Logger LOG = Logger.getInstance(SelectCommandDialog.class);

    private final JPanel root;
    private JBTextField inputText;
    private JBTextField commandDetails;
    private JBList<Command> commandList;
    private Command selectedCommand;

    private final Project project;
    private final Integer sshId;

    public SelectCommandDialog(Project project, Integer sshId) {
        super(project);
        this.sshId = sshId;
        this.project = project;

        initInput();
        initCommandList();
        initCommandDetail();
        List<Command> commands = loadCommandList();

        // 绑定监听时间，实现模糊搜索
        bindInputChangeListener(commands);
        root = FormBuilder.createFormBuilder()
                .addLabeledComponent(MessagesBundle.getText("dialog.command.select.show"), inputText, true)
                .addComponentFillVertically(createCommandToolbarPanel(), 8)
                .addComponent(commandDetails)
                .getPanel();
        root.setPreferredSize(new Dimension(UiUtil.screenWidth() / 2, 600));

        setTitle(MessagesBundle.getText("dialog.command.select.frame"));
        init();
    }

    private void bindInputChangeListener(List<Command> commands) {
        inputText.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                // 根据查询条件更新命令列表
                // 当输入内容为空时，显示所有命令
                String text = inputText.getText();
                if (text.isEmpty() || text.isBlank()){
                    // 重置commonList
                    commandList.setListData(commands.toArray(new Command[0]));
                } else {
                    List<Command> searchList = commands.stream().filter(command -> Strings.contains(command.getTitle(), text)).toList();
                    // 预先构建名称到索引的映射
                    Map<String, Integer> nameToIndex = new HashMap<>();
                    for (int i = 0; i < searchList.size(); i++) {
                        nameToIndex.put(searchList.get(i).getTitle(), i);
                    }

                    // 快速查找
                    Integer index = nameToIndex.get(text);
                    if (index != null) {
                        LOG.info("find command: " + text);
                        commandList.setSelectedIndex(index);
                    }
                    commandList.setListData(searchList.toArray(new Command[0]));
                    commandList.requestFocusInWindow();
                    commandList.repaint();
                }
            }
        });
    }
    
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return root;
    }

    @Override
    protected void doOKAction() {
        // 点击 OK 按钮时执行命令
        runCommand();
    }

    /**
     * 覆盖 OK 按钮文本，显示为"运行"
     */
    protected String getOKButtonText() {
        return "运行";
    }

    @Override
    public void onApplicationEvent(CommandAddEvent event) {
        loadCommandList();
    }

    private void initInput() {
        inputText = new JBTextField();
    }

    private void initCommandDetail() {
        commandDetails = new JBTextField();
        commandDetails.setEditable(false);
    }

    private void initCommandList() {
        commandList = new JBList<>();
        commandList.setCellRenderer(new CommandColoredListCellRenderer());
        commandList.addListSelectionListener(e -> {
            Command command = commandList.getSelectedValue();
            if (command != null) {
                selectedCommand = command;
                commandDetails.setText(command.getContent());
            } else {
                commandDetails.setText("");
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
                .addExtraAction(new CopyCommandAction(() -> selectedCommand))
                .createPanel();
    }

    private boolean isEditable() {
        Command selectedValue = commandList.getSelectedValue();
        return !(selectedValue instanceof SeparatorCommand);
    }

    public List<Command> loadCommandList() {
        List<Command> commands = ConfigHelper.getAllCommands();

        commandList.setListData(commands.toArray(new Command[0]));
        return commands;
    }

    private void runCommand() {
        Command cmd = commandList.getSelectedValue();
        if (Objects.isNull(cmd) || cmd instanceof SeparatorCommand || cmd instanceof NoneCommand) {
            return;
        }
        SshServer server = ConfigHelper.getSshServerById(sshId);

        boolean needPassword = AuthType.needPassword(server.getAuthType());
//        server = UiUtil.requestPasswordIfNecessary(server);
        if (!needPassword || StringUtil.isNotEmpty(server.getPassword())) {
            CommandUtil.executeCommand(project, cmd, server, this);
        } else {
            UiUtil.getUserInputAsync().thenAccept(password -> {
                server.setPassword(password);
                // 继续后续业务操作
                if (StringUtil.isEmpty(server.getPassword())) {
                    return;
                }
                CommandUtil.executeCommand(project, cmd, server, this);
            }).exceptionally(throwable -> {
                // 处理异常情况
                return null;
            });
        }
    }

    private class RunCommandAction extends AnAction {

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