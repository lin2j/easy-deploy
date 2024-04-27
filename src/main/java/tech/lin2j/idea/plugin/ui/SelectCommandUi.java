package tech.lin2j.idea.plugin.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
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
import tech.lin2j.idea.plugin.uitl.UiUtil;

import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;
import java.util.Objects;

/**
 * @author linjinjia
 * @date 2022/4/27 14:06
 */
public class SelectCommandUi extends DialogWrapper implements ApplicationListener<CommandAddEvent> {
    private JTextField cmdShow;
    private JButton editBtn;
    private JButton deleteBtn;
    private JButton addBtn;
    private JButton runBtn;
    private JButton closeBtn;
    private JPanel mainPanel;
    private JList<Command> cmdList;
    private JScrollPane cmdScrollPanel;

    private final Project project;
    private final Integer sshId;

    public SelectCommandUi(Project project, Integer sshId) {
        super(true);
        this.project = project;
        this.sshId = sshId;
        setTitle("Select Command");

        uiInit();
        init();
    }

    private void uiInit() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        mainPanel.setMinimumSize(new Dimension(tk.getScreenSize().width / 2, 0));

        loadCommandList();

        cmdList.setCellRenderer(new CommandColoredListCellRenderer());
        cmdScrollPanel.setMinimumSize(new Dimension(600, 300));

        addBtn.addActionListener(e -> {
            new AddCommandUi(sshId, null, null, null, null).showAndGet();
        });

        editBtn.addActionListener(e -> {
            Command cmd = cmdList.getSelectedValue();
            if (cmd == null) {
                return;
            }
            new AddCommandUi(sshId, cmd.getId(), cmd.getTitle(), cmd.getDir(), cmd.getContent()).showAndGet();
        });

        deleteBtn.addActionListener(e -> {
            Command cmd = cmdList.getSelectedValue();
            if (cmd == null) {
                return;
            }
            boolean confirm = UiUtil.deleteConfirm(cmd.toString());
            if (confirm) {
                ConfigHelper.removeCommand(cmd);
                loadCommandList();
            }
        });

        runBtn.addActionListener(e -> {
            Command cmd = cmdList.getSelectedValue();
            if (Objects.isNull(cmd)) {
                return;
            }
            SshServer server = ConfigHelper.getSshServerById(cmd.getSshId());

            boolean needPassword = AuthType.needPassword(server.getAuthType());
            server = UiUtil.requestPasswordIfNecessary(server);
            if (needPassword && StringUtil.isEmpty(server.getPassword())) {
                return;
            }
            CommandUtil.executeAndShowMessages(project, cmd, null, server, this);
        });

        closeBtn.addActionListener(e -> close(CANCEL_EXIT_CODE));

        cmdList.addListSelectionListener(e -> {
            Command command = cmdList.getSelectedValue();
            if (command != null) {
                cmdShow.setText(command.toString());
            }
        });

    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel dialog = new JPanel(new BorderLayout());
        mainPanel.setVisible(true);
        dialog.add(mainPanel, BorderLayout.CENTER);
        return dialog;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{};
    }

    @Override
    public void onApplicationEvent(CommandAddEvent event) {
        loadCommandList();
    }

    public void loadCommandList() {
        List<Command> commands = ConfigHelper.getCommandsBySshId(sshId);
        if (commands.size() == 0) {
            cmdShow.setText("");
            cmdList.setListData(new Command[0]);
            return;
        }
        cmdList.setListData(commands.toArray(new Command[0]));
    }

}