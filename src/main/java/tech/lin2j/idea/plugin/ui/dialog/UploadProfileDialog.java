package tech.lin2j.idea.plugin.ui.dialog;

import com.google.gson.Gson;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.TextTransferable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.enums.AuthType;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.event.ApplicationListener;
import tech.lin2j.idea.plugin.model.Command;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.NoneCommand;
import tech.lin2j.idea.plugin.model.UploadProfile;
import tech.lin2j.idea.plugin.model.event.UploadProfileAddEvent;
import tech.lin2j.idea.plugin.model.event.UploadProfileSelectedEvent;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.uitl.CommandUtil;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;
import tech.lin2j.idea.plugin.uitl.UiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;

import static tech.lin2j.idea.plugin.enums.Constant.LOCAL_FILE_INFO_SEPARATOR;
import static tech.lin2j.idea.plugin.enums.Constant.STR_TRUE;
import static tech.lin2j.idea.plugin.ui.render.CommandColoredListCellRenderer.TEXT_PADDING;

/**
 * @author linjinjia
 * @date 2024/5/5 00:16
 */
public class UploadProfileDialog extends DialogWrapper implements ApplicationListener<UploadProfileAddEvent> {
    private final JPanel root;
    private JPanel profileContainer;
    private ComboBox<UploadProfile> profileBox;
    private JButton actionButton;
    private JBLabel hostLabel;
    private JBLabel fileLabel;
    private JBLabel excludeLabel;
    private JBLabel locationLabel;
    private JPanel preCommandLabelContainer;
    private SimpleColoredComponent preCommandLabel;
    private JPanel postCommandLabelContainer;
    private SimpleColoredComponent postCommandLabel;

    private final Project project;
    private final SshServer server;

    public UploadProfileDialog(@NotNull Project project, SshServer server) {
        super(project);
        this.project = project;
        this.server = server;

        initLabel();
        initActionButton();
        initProfileContainer();
        initCommandLabelContainers();
        reloadProfileBox();

        root = FormBuilder.createFormBuilder()
                .addLabeledComponent(MessagesBundle.getText("dialog.upload.profile"), profileContainer, 20)
                .addLabeledComponent(MessagesBundle.getText("dialog.upload.host"), hostLabel, 20)
                .addLabeledComponent(MessagesBundle.getText("dialog.upload.file"), fileLabel, 20)
                .addLabeledComponent(MessagesBundle.getText("dialog.upload.exclude"), excludeLabel, 20)
                .addLabeledComponent(MessagesBundle.getText("dialog.upload.location"), locationLabel, 20)
                .addLabeledComponent(MessagesBundle.getText("dialog.upload.pre-command"), preCommandLabelContainer, 20)
                .addLabeledComponent(MessagesBundle.getText("dialog.upload.post-command"), postCommandLabelContainer, 20)
                .getPanel();
        root.setPreferredSize(new Dimension(UiUtil.screenWidth() / 2, 0));

        setOKButtonText("Upload");
        setTitle(MessagesBundle.getText("dialog.upload.frame"));
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return root;
    }

    @Override
    public void onApplicationEvent(UploadProfileAddEvent event) {
        reloadProfileBox();
    }

    @Override
    protected void doOKAction() {
        UploadProfile profile = (UploadProfile) profileBox.getSelectedItem();
        if (profile == null) {
            return;
        }
        profile.setSelected(true);

        boolean needPassword = AuthType.needPassword(server.getAuthType());
        SshServer sshServer = UiUtil.requestPasswordIfNecessary(server);
        if (needPassword && StringUtil.isEmpty(sshServer.getPassword())) {
            return;
        }

        CommandUtil.executeUpload(project, profile, sshServer, this);
        ApplicationContext.getApplicationContext().publishEvent(new UploadProfileSelectedEvent(profile));

        super.doOKAction();
    }

    private void initLabel() {
        hostLabel = new JBLabel();
        fileLabel = new JBLabel();
        excludeLabel = new JBLabel();
        locationLabel = new JBLabel();
    }

    private void initCommandLabelContainers() {
        preCommandLabel = new SimpleColoredComponent();
        postCommandLabel = new SimpleColoredComponent();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        preCommandLabelContainer = new JPanel(new GridBagLayout());
        preCommandLabelContainer.add(preCommandLabel, gbc);
        
        postCommandLabelContainer = new JPanel(new GridBagLayout());
        postCommandLabelContainer.add(postCommandLabel, gbc);
    }

    private void initProfileContainer() {
        profileBox = new ComboBox<>();
        profileBox.addItemListener(e -> updateProfileInfo((UploadProfile) e.getItem()));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = JBUI.insetsRight(10);
        profileContainer = new JPanel(new GridBagLayout());
        profileContainer.add(profileBox, gbc);
        profileContainer.add(actionButton);
    }

    private void initActionButton() {
        actionButton = new JButton();
        actionButton.setText(MessagesBundle.getText("dialog.upload.actions") + " ▼");
        actionButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JPopupMenu menu = new JPopupMenu();
                menu.add(new JMenuItem(new AbstractAction(MessagesBundle.getText("dialog.upload.actions.add")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        UploadProfile profile = new UploadProfile();
                        profile.setSshId(server.getId());
                        new AddUploadProfileDialog(project, profile).showAndGet();
                    }
                }));
                menu.add(new JMenuItem(new AbstractAction(MessagesBundle.getText("dialog.upload.actions.edit")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        UploadProfile profile = (UploadProfile) profileBox.getSelectedItem();
                        if (profile == null) {
                            return;
                        }
                        new AddUploadProfileDialog(project, profile).showAndGet();
                    }
                }));
                menu.add(new JMenuItem(new AbstractAction(MessagesBundle.getText("dialog.upload.actions.remove")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        UploadProfile profile = (UploadProfile) profileBox.getSelectedItem();
                        if (profile == null) {
                            return;
                        }
                        if (UiUtil.deleteConfirm(profile.toString())) {
                            ConfigHelper.removeUploadProfile(profile);
                            reloadProfileBox();
                        }
                    }
                }));
                menu.add(new JMenuItem(new AbstractAction(MessagesBundle.getText("dialog.upload.actions.copy")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        UploadProfile profile = (UploadProfile) profileBox.getSelectedItem();
                        if (profile == null) {
                            return;
                        }
                        Gson gson = new Gson();
                        String json = gson.toJson(profile);
                        CopyPasteManager.getInstance().setContents(new TextTransferable(json));
                    }
                }));
                menu.show(actionButton, 0, actionButton.getHeight());
            }
        });
    }

    private void updateProfileInfo(UploadProfile profile) {
        hostLabel.setText(server.getIp() + ":" + server.getPort());
        String[] split = profile.getFile().split(LOCAL_FILE_INFO_SEPARATOR);
        boolean useRegex = split.length == 2 && Objects.equals(split[1], STR_TRUE);
        fileLabel.setIcon(useRegex ? AllIcons.Actions.Regex : null);
        fileLabel.setText(split[0]);
        excludeLabel.setText(profile.getExclude());
        locationLabel.setText(profile.getLocation());

        // Clear both command labels
        preCommandLabel.clear();
        postCommandLabel.clear();
        
        // Handle backward compatibility: if commandId exists but pre/postCommandId don't, migrate to postCommandId
        Integer preCommandId = profile.getPreCommandId();
        Integer postCommandId = profile.getPostCommandId();
        if (preCommandId == null && postCommandId == null && profile.getCommandId() != null) {
            postCommandId = profile.getCommandId();
        }
        
        // Display pre-upload command
        if (preCommandId != null) {
            Command preCmd = ConfigHelper.getCommandById(preCommandId);
            if (preCmd == null) {
                preCmd = NoneCommand.INSTANCE;
                profile.setPreCommandId(null);
            } else {
                if (StringUtil.isNotEmpty(preCmd.getTitle())) {
                    preCommandLabel.append(preCmd.getTitle() + TEXT_PADDING);
                }
                String overrideDir = (profile.getUseUploadPath() != null && profile.getUseUploadPath()) ? profile.getLocation() : null;
                preCommandLabel.append(preCmd.toDisplayString(overrideDir), SimpleTextAttributes.GRAY_ATTRIBUTES);
            }
        }
        
        // Display post-upload command
        if (postCommandId != null) {
            Command postCmd = ConfigHelper.getCommandById(postCommandId);
            if (postCmd == null) {
                postCmd = NoneCommand.INSTANCE;
                profile.setPostCommandId(null);
            } else {
                if (StringUtil.isNotEmpty(postCmd.getTitle())) {
                    postCommandLabel.append(postCmd.getTitle() + TEXT_PADDING);
                }
                String overrideDir = (profile.getUseUploadPath() != null && profile.getUseUploadPath()) ? profile.getLocation() : null;
                postCommandLabel.append(postCmd.toDisplayString(overrideDir), SimpleTextAttributes.GRAY_ATTRIBUTES);
            }
        }
    }

    private void reloadProfileBox() {
        profileBox.removeAllItems();
        List<UploadProfile> profiles = ConfigHelper.getUploadProfileBySshId(server.getId());
        if (!profiles.isEmpty()) {
            int i = 0;
            boolean hasSelected = false;
            for (UploadProfile profile : profiles) {
                profileBox.addItem(profile);
                if (profile.getSelected()) {
                    profileBox.setSelectedIndex(i);
                    updateProfileInfo(profile);
                    hasSelected = true;
                }
                i++;
            }
            if (!hasSelected) {
                updateProfileInfo(profiles.get(0));
            }
        } else {
            hostLabel.setText(server.getIp() + ":" + server.getPort());
            fileLabel.setText("");
            locationLabel.setText("");
            preCommandLabel.clear();
            postCommandLabel.clear();
        }
    }
}