package tech.lin2j.idea.plugin.ui.ftp;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBTabbedPane;
import icons.MyIcons;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ui.ftp.container.LocalFileTableContainer;
import tech.lin2j.idea.plugin.ui.ftp.container.RemoteFileTableContainer;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.io.IOException;

/**
 * @author linjinjia
 * @date 2024/4/4 09:30
 */
public class FTPConsole {

    private JPanel root;

    private final Project project;
    private final SshServer server;

    public FTPConsole(Project project, SshServer server) throws IOException {
        this.project = project;
        this.server = server;
        init();
    }

    public void init() {
        root = new JPanel();
        root.setLayout(new BorderLayout());

        LocalFileTableContainer localContainer = new LocalFileTableContainer(project);
        RemoteFileTableContainer remoteContainer = new RemoteFileTableContainer(project, server);

        JBSplitter fileWindows = new JBSplitter(false, "", 0.5f);
        fileWindows.setFirstComponent(localContainer);
        fileWindows.setSecondComponent(remoteContainer);

        JBSplitter mainPanel = new JBSplitter(true, "", 0.6f);
        mainPanel.setFirstComponent(fileWindows);
        ProgressTable progressTable = new ProgressTable(localContainer, remoteContainer);

        JBTabbedPane transferPane = new JBTabbedPane();
        transferPane.addTab("Transfer", MyIcons.TRANSFER, progressTable);
        transferPane.addTab("Log" , AllIcons.Debugger.Console, new ConsoleViewImpl(project, false));

        mainPanel.setSecondComponent(transferPane);

        ApplicationContext.getApplicationContext().addApplicationListener(progressTable);

        root.add(mainPanel);
    }


    public JPanel createUi() {
        return root;
    }

}