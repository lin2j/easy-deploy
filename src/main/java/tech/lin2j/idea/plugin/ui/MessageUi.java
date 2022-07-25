package tech.lin2j.idea.plugin.ui;

import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import tech.lin2j.idea.plugin.domain.model.Command;
import tech.lin2j.idea.plugin.domain.model.event.CommandExecuteEvent;
import tech.lin2j.idea.plugin.event.ApplicationListener;
import tech.lin2j.idea.plugin.ssh.SshServer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author linjinjia
 * @date 2022/4/29 09:58
 */
public class MessageUi implements ApplicationListener<CommandExecuteEvent> {
    private JPanel mainPanel;
    private JTextArea retContent;
    private JScrollPane retScroll;

    public MessageUi() {
        retContent.setEditable(false);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    @Override
    public void onApplicationEvent(CommandExecuteEvent event) {
        SshServer server = event.getServer();
        String title = String.format("executing command on %s:%s", server.getIp(), server.getPort());
        ToolWindow messages = ToolWindowManager.getInstance(event.getProject()).getToolWindow("Messages");
        messages.setTitle(title);
        messages.activate(null);

        String time = String.format("%tF %<tT", System.currentTimeMillis());
        Command cmd = event.getCommand();
        retContent.setText(String.format("[INFO] %s user custom command: {%s}\n", time, cmd.generateCmdLine()));
        retContent.append(event.getExecResult());
    }

}