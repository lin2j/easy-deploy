package tech.lin2j.idea.plugin.ui;

import tech.lin2j.idea.plugin.domain.model.Command;
import tech.lin2j.idea.plugin.domain.model.event.CommandExecuteEvent;
import tech.lin2j.idea.plugin.event.ApplicationListener;

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
        Command cmd = event.getCommand();
        retContent.setText(String.format("[INFO] user custom command: {%s}\n", cmd.generateCmdLine()));
        retContent.append(event.getExecResult());
    }

}