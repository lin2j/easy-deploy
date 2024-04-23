package tech.lin2j.idea.plugin.ui;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.CollectionComboBoxModel;
import org.jdesktop.swingx.prompt.PromptSupport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.event.TableRefreshEvent;
import tech.lin2j.idea.plugin.enums.AuthType;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.factory.SshServiceFactory;
import tech.lin2j.idea.plugin.service.ISshService;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.SshStatus;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.function.Consumer;

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
    private JTextField descInput;
    private JRadioButton passRadio;
    private JRadioButton pemPrivateRadio;
    private JTextField pemPrivateKeyInput;
    private JLabel pemPrivateKeyLabel;
    private JLabel passwordLabel;
    private ComboBox<String> tagComboBox;
    private ButtonGroup authTypeGroup;

    private final Project project;
    private final SshServer server;
    private final ISshService sshClient = SshServiceFactory.getSshService();

    public HostUi(Project project, SshServer server) {
        super(true);
        this.project = project;
        this.server = server;
        uiInit();
        setTitle("Add Host");
        init();
    }

    private void uiInit() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        mainPanel.setMinimumSize(new Dimension(tk.getScreenSize().width / 3, 0));

        // default
        passRadio.setSelected(true);
        this.changePemPrivateKeyAuthTypeEnable(false);

        tagComboBox.setModel(new CollectionComboBoxModel<>(ConfigHelper.getServerTags()));

        if (server != null) {
            ipInput.setText(server.getIp());
            portInput.setText(server.getPort().toString());
            userInput.setText(server.getUsername());
            passInput.setText(server.getPassword());
            tagComboBox.setSelectedItem(server.getTag());
            descInput.setText(server.getDescription());
            pemPrivateKeyInput.setText(server.getPemPrivateKey());
            if (AuthType.needPassword(server.getAuthType())) {
                passRadio.setSelected(true);
                this.changePasswordAuthTypeEnable(true);
                this.changePemPrivateKeyAuthTypeEnable(false);
            } else {
                pemPrivateRadio.setSelected(true);
                this.changePasswordAuthTypeEnable(false);
                this.changePemPrivateKeyAuthTypeEnable(true);
            }
        }

        passInput.setEchoChar('·');
        showPass.addActionListener((e) -> {
            if (showPass.isSelected()) {
                passInput.setEchoChar((char) 0);
            } else {
                passInput.setEchoChar('·');
            }
        });

        passRadio.setActionCommand(AuthType.PASSWORD.toString());
        passRadio.addActionListener(e -> {
            this.changePasswordAuthTypeEnable(true);
            this.changePemPrivateKeyAuthTypeEnable(false);
        });

        PromptSupport.setPrompt("pem private key file path", pemPrivateKeyInput);
        pemPrivateRadio.setActionCommand(AuthType.PEM_PRIVATE_KEY.toString());
        pemPrivateRadio.addActionListener(e -> {
            this.changePasswordAuthTypeEnable(false);
            this.changePemPrivateKeyAuthTypeEnable(true);
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
            SshServer sshServer = new SshServer();
            if (setServerInfo(sshServer, false, true)) {
                return;
            }
            String title = String.format("Testing %s:%s", sshServer.getIp(), sshServer.getPort());
            ProgressManager.getInstance().run(new Task.Backgroundable(project, title) {
                SshStatus status = null;

                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    indicator.setIndeterminate(false);
                    status = sshClient.isValid(sshServer);
                    indicator.setFraction(1.0);
                }

                @Override
                public void onFinished() {
                    if (status.isSuccess()) {
                        Messages.showMessageDialog("Test success", "Test", Messages.getInformationIcon());
                        ApplicationContext.getApplicationContext().publishEvent(new TableRefreshEvent());
                    } else {
                        Messages.showErrorDialog(status.getMessage(), "Test");
                    }
                }
            });
        }
    }

    class ConfirmActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (server != null) {
                if (setServerInfo(server, false, false)) {
                    return;
                }
            } else {
                SshServer sshServer = new SshServer();
                if (setServerInfo(sshServer, true, false)) {
                    return;
                }
                ConfigHelper.addSshServer(sshServer);
            }
            ApplicationContext.getApplicationContext().publishEvent(new TableRefreshEvent());
            close(OK_EXIT_CODE);
        }
    }

    /**
     * return true if parameter required is missing
     */
    private boolean setServerInfo(SshServer server, boolean needId, boolean test) {
        boolean miss = false;
        if (needId) {
            server.setId(ConfigHelper.maxSshServerId() + 1);
        }
        if (setText(ipInput, true, server::setIp)) {
            return true;
        }
        if (setText(portInput, true, port -> server.setPort(Integer.parseInt(port)))) {
            return true;
        }
        if (setText(userInput, true, server::setUsername)) {
            return true;
        }
        if (passRadio.isSelected()) {
            if (setText(passInput, test, server::setPassword)) {
                return true;
            }
            server.setAuthType(AuthType.PASSWORD.getCode());
            server.setPemPrivateKey(null);
        }
        if (pemPrivateRadio.isSelected()) {
            if (setText(pemPrivateKeyInput, test, server::setPemPrivateKey)) {
                return true;
            }
            server.setPassword(null);
            server.setAuthType(AuthType.PEM_PRIVATE_KEY.getCode());
        }
        setText(descInput, false, server::setDescription);
        server.setTag(Objects.toString(tagComboBox.getSelectedItem()));
        return miss;
    }

    /**
     * return true if the text obtained from the input is empty
     */
    private boolean setText(JTextComponent input, boolean required, Consumer<String> action) {
        String text = input.getText();
        if (required && StringUtil.isEmpty(text)) {
            SwingUtilities.invokeLater(input::requestFocus);
            return true;
        }
        action.accept(text);
        return false;
    }

    private void changePasswordAuthTypeEnable(boolean enabled) {
        this.passwordLabel.setEnabled(enabled);
        this.passInput.setEnabled(enabled);
        this.showPass.setEnabled(enabled);
        this.pack();
    }

    private void changePemPrivateKeyAuthTypeEnable(boolean enabled) {
        this.pemPrivateKeyInput.setEnabled(enabled);
        this.pemPrivateKeyLabel.setEnabled(enabled);
        this.pack();
    }


}