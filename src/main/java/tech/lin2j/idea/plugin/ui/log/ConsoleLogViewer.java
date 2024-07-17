package tech.lin2j.idea.plugin.ui.log;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import tech.lin2j.idea.plugin.model.Command;
import tech.lin2j.idea.plugin.model.event.CommandExecuteEvent;
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
public class ConsoleLogViewer implements ApplicationListener<CommandExecuteEvent>, Disposable {

    private static final String ID = "ConsoleLogViewer";
    private static final String TITLE = "Console Log";

    private final Project project;
    private ConsoleViewImpl console;

    private JPanel root;

    public ConsoleLogViewer(Project project) {
        this.project = project;
        initRoot();
    }

    public JPanel getRoot() {
        return root;
    }

    public ConsoleView getConsoleView() {
        return console;
    }

    private void initRoot() {
        root = new JPanel(new BorderLayout());
        initConsoleView();
        root.add(console, BorderLayout.CENTER);
    }

    /**
     * Code to initialize the console window and its toolbar.
     */
    private void initConsoleView() {
        this.console = new ConsoleViewImpl(project, false);
        //
        final RunContentDescriptor descriptor = new RunContentDescriptor(console, null, root, TITLE);
        Disposer.register(this, descriptor);

        // must call getComponent before createConsoleActions()
        final JComponent consoleViewComponent = console.getComponent();

        // action like 'Clean All'
        final DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.addAll(console.createConsoleActions());

        final ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ID, actionGroup, false);
        toolbar.setTargetComponent(consoleViewComponent);

        final JComponent ui = descriptor.getComponent();
        ui.add(consoleViewComponent, BorderLayout.CENTER);
        ui.add(toolbar.getComponent(), BorderLayout.WEST);

        // Add a border to make things look nicer.
        consoleViewComponent.setBorder(BorderFactory.createEmptyBorder());
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
        console.print(msg, ConsoleViewContentType.NORMAL_OUTPUT);
    }

    @Override
    public void dispose() {

    }
}