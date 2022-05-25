package tech.lin2j.idea.plugin.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.CommandUtil;
import tech.lin2j.idea.plugin.domain.model.Command;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.SshServer;
import tech.lin2j.idea.plugin.domain.model.event.CommandAddEvent;
import tech.lin2j.idea.plugin.domain.model.event.CommandExecuteEvent;
import tech.lin2j.idea.plugin.event.ApplicationListener;
import tech.lin2j.idea.plugin.service.SshService;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

    private SshService sshService = SshService.getInstance();

    private static final Logger LOG = Logger.getInstance(SelectCommandUi.class);
    private static final BlockingQueue<CommandExecuteEvent> EVENT_QUEUE = new LinkedBlockingQueue<>(100);

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

        cmdScrollPanel.setMinimumSize(new Dimension(600, 300));

        addBtn.addActionListener(e -> {
            new AddCommandUi(sshId, null, null, null).showAndGet();
        });

        deleteBtn.addActionListener(e -> {
            Command command = cmdList.getSelectedValue();
            ConfigHelper.removeCommand(command);
            loadCommandList();
        });

        editBtn.addActionListener(e -> {
            Command cmd = cmdList.getSelectedValue();
            new AddCommandUi(sshId, cmd.getId(), cmd.getDir(), cmd.getContent()).showAndGet();
        });

        deleteBtn.addActionListener(e -> {
            Command command = cmdList.getSelectedValue();
            ConfigHelper.removeCommand(command);
            loadCommandList();
        });

        runBtn.addActionListener(e -> {
            Command cmd = cmdList.getSelectedValue();
            SshServer server = ConfigHelper.getSshServerById(cmd.getSshId());
            CommandUtil.executeAndShowMessages(project, cmd, server, this);
        });

        closeBtn.addActionListener(e -> close(CANCEL_EXIT_CODE));

        cmdList.addListSelectionListener(e -> {
            Command command = cmdList.getSelectedValue();
            cmdShow.setText(command.toString());
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
            return;
        }
        cmdList.setListData(commands.toArray(new Command[0]));
    }

}