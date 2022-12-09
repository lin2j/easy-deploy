package tech.lin2j.idea.plugin.ui;

import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
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
        boolean append = event.getSignal() == CommandExecuteEvent.SIGNAL_APPEND;
        boolean clear = event.getSignal() == CommandExecuteEvent.SIGNAL_CLEAR;
        boolean firstMsg = event.getIndex() == 0;

        boolean showMessageUi = clear || (append && firstMsg);
        if (showMessageUi) {
            ToolWindow deployToolWindow = ToolWindowManager.getInstance(event.getProject()).getToolWindow("Deploy");
            deployToolWindow.activate(null);
            Content messages = deployToolWindow.getContentManager().findContent("Messages");
            deployToolWindow.getContentManager().setSelectedContent(messages);
        }

        if (clear) {
            printResult(event);
        } else if (append) {
            if (firstMsg) {
                SshServer server = event.getServer();
                String time = String.format("%tF %<tT", System.currentTimeMillis());
                String title = String.format("executing command on %s:%s\n", server.getIp(), server.getPort());
                retContent.setText(title);
                retContent.append(String.format("[INFO] %s user custom command: {%s}\n\n", time, event.getExecResult()));
            } else {
                retContent.append(event.getExecResult());
            }

        }
    }

    private void printResult(CommandExecuteEvent event) {
        SshServer server = event.getServer();
        Command cmd = event.getCommand();
        String time = String.format("%tF %<tT", System.currentTimeMillis());
        String title = String.format("executing command on %s:%s\n", server.getIp(), server.getPort());
        retContent.setText(title);
        retContent.append(String.format("[INFO] %s user custom command: {%s}\n\n", time, cmd.generateCmdLine()));
        retContent.append(event.getExecResult());
        String finishedTime = String.format("%tF %<tT", System.currentTimeMillis());
        retContent.append(String.format("\n\n[INFO] %s finished", finishedTime));
    }
}