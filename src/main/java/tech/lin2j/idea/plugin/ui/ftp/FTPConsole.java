package tech.lin2j.idea.plugin.ui.ftp;

import com.intellij.openapi.project.Project;
import com.intellij.ui.JBSplitter;
import net.schmizz.sshj.sftp.SFTPClient;
import tech.lin2j.idea.plugin.ssh.SshConnectionManager;
import tech.lin2j.idea.plugin.ssh.SshServer;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;

/**
 * @author linjinjia
 * @date 2024/4/4 09:30
 */
public class FTPConsole {

    private JPanel root;

    private final Project project;
    private final SFTPClient sftpClient;

    public FTPConsole(Project project, SshServer server) throws IOException {
        this.project = project;
        this.sftpClient = SshConnectionManager.makeSshClient(server).newSFTPClient();
        init();
    }

    public void init() {
        root = new JPanel();
        root.setLayout(new BorderLayout());

        JBSplitter fileWindows = new JBSplitter(false, "", 0.5f);
        fileWindows.setFirstComponent(new LocalFileContainer(project));
        fileWindows.setSecondComponent(new RemoteFileContainer(project, sftpClient));

        JBSplitter mainPanel = new JBSplitter(true, "", 0.6f);
        mainPanel.setFirstComponent(fileWindows);
        mainPanel.setSecondComponent(new ProgressTable());

        root.add(mainPanel);
    }


    public JPanel createUi() {
        return root;
    }

}