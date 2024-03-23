package tech.lin2j.idea.plugin.ui;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import tech.lin2j.idea.plugin.domain.model.Command;
import tech.lin2j.idea.plugin.domain.model.event.CommandExecuteEvent;
import tech.lin2j.idea.plugin.event.ApplicationListener;
import tech.lin2j.idea.plugin.ssh.SshServer;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * @author linjinjia
 * @date 2024/3/16 18:53
 */
public class CommandLogViewer implements ApplicationListener<CommandExecuteEvent>, Disposable {

    private static final String ID = "CommandLogViewer";
    private static final String TITLE = "Remote Command Execute Result";

    private final Project project;
    private ConsoleViewImpl myConsoleView;

    private JPanel mainPanel;

    public CommandLogViewer(Project project) {
        this.project = project;
    }

    public void createUIComponents() {
        this.myConsoleView = new ConsoleViewImpl(project, false);
    }

    /**
     * Code to initialize the console window and its toolbar.
     */
    private void layoutConsoleView() {
        //
        final RunContentDescriptor descriptor = new RunContentDescriptor(myConsoleView, null, mainPanel, TITLE);
        Disposer.register(this, descriptor);

        // must call getComponent before createConsoleActions()
        final JComponent consoleViewComponent = myConsoleView.getComponent();

        // action like 'Clean All'
        final DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.addAll(myConsoleView.createConsoleActions());

        final ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, false);
        toolbar.setTargetComponent(consoleViewComponent);

        final JComponent ui = descriptor.getComponent();
        ui.add(consoleViewComponent, BorderLayout.CENTER);
        ui.add(toolbar.getComponent(), BorderLayout.WEST);

        // Add a border to make things look nicer.
        consoleViewComponent.setBorder(BorderFactory.createEmptyBorder());
    }


    public JPanel getMainPanel() {
        layoutConsoleView();
        return mainPanel;
    }
    @Override
    public void onApplicationEvent(CommandExecuteEvent event) {
        boolean append = event.getSignal() == CommandExecuteEvent.SIGNAL_APPEND;
        boolean clear = event.getSignal() == CommandExecuteEvent.SIGNAL_CLEAR;
        boolean firstMsg = event.getIndex() != null && event.getIndex() == 0;

        boolean showMessageUi = clear || (append && firstMsg);
        if (showMessageUi) {
            ToolWindow deployToolWindow = ToolWindowManager.getInstance(event.getProject()).getToolWindow("Deploy");
            deployToolWindow.activate(null);
            Content messages = deployToolWindow.getContentManager().findContent("Console");
            deployToolWindow.getContentManager().setSelectedContent(messages);
        }

        if (clear) {
            printResult(event);
        } else if (append) {
            if (firstMsg) {
                SshServer server = event.getServer();
                print(String.format("Executing command on %s:%s", server.getIp(), server.getPort()));
                print(String.format("User custom command: {%s}", event.getExecResult()));
            } else {
                print(event.getExecResult());
            }
        }
    }

    private void printResult(CommandExecuteEvent event) {
        SshServer server = event.getServer();
        Command cmd = event.getCommand();
        String title = String.format("Executing command on %s:%s", server.getIp(), server.getPort());
        print(title);
        if (cmd != null) {
            print(String.format("User custom command: {%s}",cmd.generateCmdLine()));
        }
        print(event.getExecResult());
        print("finished");
    }

    private void print(String content) {
        String time = String.format("%tF %<tT", System.currentTimeMillis());
        String msg = String.format("%s [INFO] %s\n", time, content);
        myConsoleView.print(msg, ConsoleViewContentType.NORMAL_OUTPUT);
    }

    @Override
    public void dispose() {

    }
}