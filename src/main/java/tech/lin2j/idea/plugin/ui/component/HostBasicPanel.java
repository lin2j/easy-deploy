package tech.lin2j.idea.plugin.ui.component;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.SystemProperties;
import com.intellij.util.ui.FormBuilder;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.enums.AuthType;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class HostBasicPanel {

    private final JPanel root;

    private JBTextField ipInput;
    private JBTextField portInput;
    private JBTextField userInput;
    private JBTextField descInput;

    private JBPasswordField passwdInput;
    private TextFieldWithBrowseButton privateKeyInput;
    private ComboBox<String> tagComboBox;

    private JBRadioButton passwdRadio;
    private JBRadioButton privateKeyRadio;
    private JPanel authTypeContainer;
    private JPanel testConnectContainer;

    private final SshServer contentProvider;
    private final Project project;
    private final JButton testButton;

    public HostBasicPanel(Project project, SshServer contentProvider, JButton testButton) {
        this.contentProvider = contentProvider;
        this.project = project;
        this.testButton = testButton;

        initInput();
        initAuthTypeContainer();
        initTagComboBox();
        initTestConnectContainer();
        setContent();

        root = FormBuilder.createFormBuilder()
                .addLabeledComponent(MessagesBundle.getText("dialog.panel.host.basic.ip"), ipInput)
                .addLabeledComponent(MessagesBundle.getText("dialog.panel.host.basic.port"), portInput)
                .addLabeledComponent(MessagesBundle.getText("dialog.panel.host.basic.user"), userInput)
                .addLabeledComponent(MessagesBundle.getText("dialog.panel.host.basic.auth-type"), authTypeContainer)
                .addLabeledComponent(MessagesBundle.getText("dialog.panel.host.basic.password"), passwdInput)
                .addLabeledComponent(MessagesBundle.getText("dialog.panel.host.basic.private-key"), privateKeyInput)
                .addLabeledComponent(MessagesBundle.getText("dialog.panel.host.basic.tag"), tagComboBox)
                .addLabeledComponent(MessagesBundle.getText("dialog.panel.host.basic.description"), descInput)
                .addComponent(testConnectContainer)
                .getPanel();
    }

    public JPanel createUI() {
        return root;
    }

    /**
     * save server information from panel
     *
     * @return return false when required field is blank
     */
    public boolean saveServerInfo(SshServer server, boolean test) {
        return !setServerInfo(server, server.getId() == null, test);
    }

    private void initInput() {
        ipInput = new JBTextField();
        portInput = new JBTextField();
        userInput = new JBTextField();
        descInput = new JBTextField();
        passwdInput = new JBPasswordField();

        privateKeyInput = new TextFieldWithBrowseButton();
        privateKeyInput.addActionListener(e -> {
            FileChooserDescriptor descriptor = allButNoMultipleChoose();
            VirtualFile virtualFile = FileChooser.chooseFile(descriptor, privateKeyInput, project, getSshDir());
            if (virtualFile != null) {
                privateKeyInput.setText(virtualFile.getPath());
            }
        });
    }

    private void initAuthTypeContainer() {
        authTypeContainer = new JPanel();
        authTypeContainer.setLayout(new GridLayout(1, 2));

        passwdRadio = new JBRadioButton(MessagesBundle.getText("dialog.panel.host.basic.auth-type.password"));
        privateKeyRadio = new JBRadioButton(MessagesBundle.getText("dialog.panel.host.basic.auth-type.private"));
        ButtonGroup group = new ButtonGroup();
        group.add(passwdRadio);
        group.add(privateKeyRadio);

        passwdRadio.addActionListener(e -> {
            passwdInput.setEnabled(true);
            privateKeyInput.setEnabled(false);
        });

        privateKeyRadio.addActionListener(e -> {
            passwdInput.setEnabled(false);
            privateKeyInput.setEnabled(true);
        });

        authTypeContainer.add(passwdRadio);
        authTypeContainer.add(privateKeyRadio);
    }

    private void initTagComboBox() {
        tagComboBox = new ComboBox<>();
        List<String> tags = new ArrayList<>();
        tags.add("");
        tags.addAll(ConfigHelper.getServerTags());
        tagComboBox.setModel(new CollectionComboBoxModel<>(tags));
    }

    private void initTestConnectContainer() {
        testConnectContainer = new JPanel(new BorderLayout());
        testConnectContainer.add(testButton, BorderLayout.EAST);
    }

    private void setContent() {
        // default
        passwdRadio.setSelected(true);
        passwdRadio.doClick();
        if (contentProvider != null) {
            ipInput.setText(contentProvider.getIp());
            portInput.setText(contentProvider.getPort().toString());
            userInput.setText(contentProvider.getUsername());
            passwdInput.setText(contentProvider.getPassword());
            tagComboBox.setSelectedItem(contentProvider.getTag());
            descInput.setText(contentProvider.getDescription());
            privateKeyInput.setText(contentProvider.getPemPrivateKey());
            if (AuthType.needPassword(contentProvider.getAuthType())) {
                passwdRadio.setSelected(true);
                passwdRadio.doClick();
            } else {
                privateKeyRadio.setSelected(true);
                privateKeyRadio.doClick();
            }
        }
    }

    private VirtualFile getSshDir() {
        String dir = SystemProperties.getUserHome() + "/.ssh";
        return LocalFileSystem.getInstance().findFileByPath(dir);
    }

    private FileChooserDescriptor allButNoMultipleChoose() {
        return new FileChooserDescriptor(true, true, true, true, true, false);
    }

    /**
     * return true if parameter required is missing
     */
    private boolean setServerInfo(SshServer server, boolean needId, boolean test) {
        boolean miss = false;
        if (needId) {
            server.setId(ConfigHelper.maxSshServerId() + 1);
            server.setUid(UUID.randomUUID().toString());
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
        if (passwdRadio.isSelected()) {
            if (setText(passwdInput, test, server::setPassword)) {
                return true;
            }
            server.setAuthType(AuthType.PASSWORD.getCode());
            server.setPemPrivateKey(null);
        }
        if (privateKeyRadio.isSelected()) {
            if (setText(privateKeyInput.getTextField(), true, server::setPemPrivateKey)) {
                return true;
            }
            server.setAuthType(AuthType.PEM_PRIVATE_KEY.getCode());
            server.setPassword(null);
        }
        setText(descInput, false, server::setDescription);
        server.setTag(Objects.toString(tagComboBox.getSelectedItem()));
        return miss;
    }

    /**
     * return true if the text obtained from the input is empty
     */
    private boolean setText(JTextField input, boolean required, Consumer<String> action) {
        boolean requestFocus = false;
        String text = input.getText();
        if (required && StringUtil.isEmpty(text)) {
            SwingUtilities.invokeLater(input::requestFocus);
            requestFocus = true;
        }
        action.accept(text);
        return requestFocus;
    }
}
