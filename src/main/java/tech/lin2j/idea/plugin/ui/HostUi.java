package tech.lin2j.idea.plugin.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.jdesktop.swingx.prompt.PromptSupport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.SshServer;
import tech.lin2j.idea.plugin.domain.model.SshStatus;
import tech.lin2j.idea.plugin.domain.model.event.TableRefreshEvent;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.service.SshService;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author linjinjia
 * @date 2022/4/25 15:17
 */
public class HostUi extends DialogWrapper {
    private JTextField ipInput;
    private JTextField portInput;
    private JTextField userInput;
    private JPasswordField passInput;
    private JCheckBox showPass;
    private JButton testBtn;
    private JButton confirmBtn;
    private JButton cancelBtn;
    private JTabbedPane tabbedPane1;
    private JPanel hostTab;
    private JPanel mainPanel;

    private final SshService sshClient = SshService.getInstance();

    public HostUi() {
        super(true);
        uiInit();
        setTitle("Add Host");
        init();
    }

    private void uiInit() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        mainPanel.setMinimumSize(new Dimension(tk.getScreenSize().width / 3, 0));
        PromptSupport.setPrompt("22", portInput);

        passInput.setEchoChar('·');
        showPass.addActionListener((e) -> {
            if (showPass.isSelected()) {
                passInput.setEchoChar((char) 0);
            } else {
                passInput.setEchoChar('·');
            }
        });

        cancelBtn.addActionListener((e) -> close(CANCEL_EXIT_CODE));

        confirmBtn.addActionListener(new ConfirmActionListener());
        testBtn.addActionListener(new TestActionListener());
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

    class TestActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            SshServer sshServer = getSshServer();
            SshStatus status = sshClient.isValid(sshServer);
            if (status.isSuccess()) {
                Messages.showMessageDialog("Test success", "Test", Messages.getInformationIcon());
                ApplicationContext.getApplicationContext().publishEvent(new TableRefreshEvent());
            } else {
                Messages.showErrorDialog(status.getMessage(), "Test");
            }
        }
    }

    class ConfirmActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            SshServer sshServer = getSshServer();
            ConfigHelper.addSshServer(sshServer);
            ApplicationContext.getApplicationContext().publishEvent(new TableRefreshEvent());
            close(OK_EXIT_CODE);
        }
    }

    private SshServer getSshServer() {
        SshServer sshServer = new SshServer();
        sshServer.setId(ConfigHelper.maxSshServerId() + 1);
        sshServer.setIp(ipInput.getText());
        sshServer.setPort(Integer.parseInt(portInput.getText()));
        sshServer.setUsername(userInput.getText());
        sshServer.setPassword(new String(passInput.getPassword()));
        return sshServer;
    }
}