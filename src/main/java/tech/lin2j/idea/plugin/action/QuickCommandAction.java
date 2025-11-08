package tech.lin2j.idea.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.terminal.ui.TerminalWidget;
import com.jediterm.terminal.TtyConnector;
import org.jetbrains.plugins.terminal.TerminalToolWindowManager;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import java.io.IOException;

/**
 * 快捷指令功能入口
 */
public class QuickCommandAction extends AnAction {

    /**
     * Creates a new action with its text, description and icon set to {@code null}.
     */
    public QuickCommandAction() {
        super(MessagesBundle.getText("dialog.quick.command.frame"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        TerminalToolWindowManager instance = TerminalToolWindowManager.getInstance(project);
        for (TerminalWidget terminalWidget : instance.getTerminalWidgets()) {
            if (terminalWidget.hasFocus()) {
                String title = terminalWidget.getTerminalTitle().getDefaultTitle();
                // 可以向终端发送文本，
                TtyConnector ttyConnector = terminalWidget.getTtyConnector();
                try {
                    if (ttyConnector != null) {
                        ttyConnector.write("echo \"Hello World\"\r");
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                Messages.showInfoMessage(project,
                        "Current Terminal: " + title ,
                        "Active Terminal Info");
            }
        }
    }
}
