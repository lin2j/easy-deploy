package tech.lin2j.idea.plugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.uitl.CommandUtil;
import tech.lin2j.idea.plugin.domain.model.Command;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.SshStatus;
import tech.lin2j.idea.plugin.domain.model.UploadProfile;
import tech.lin2j.idea.plugin.domain.model.event.UploadProfileAddEvent;
import tech.lin2j.idea.plugin.domain.model.event.UploadProfileSelectedEvent;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.event.ApplicationListener;
import tech.lin2j.idea.plugin.service.SshService;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * @author linjinjia
 * @date 2022/4/25 22:25
 */
public class UploadUi extends DialogWrapper implements ApplicationListener<UploadProfileAddEvent> {
    private JButton uploadBtn;
    private JButton cancelBtn;
    private JPanel mainPanel;
    private JLabel hostLabel;
    private JComboBox<UploadProfile> profileBox;
    private JLabel fileLabel;
    private JLabel locationLabel;
    private JLabel commandLabel;
    private JButton actionBtn;

    private final Project project;
    private final SshServer sshServer;
    private final SshService sshService = SshService.getInstance();

    public UploadUi(Project project, SshServer sshServer) {
        super(true);
        this.project = project;
        this.sshServer = sshServer;
        uiInit();
        setTitle("Upload");
        init();
    }

    private void uiInit() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        mainPanel.setMinimumSize(new Dimension(tk.getScreenSize().width / 2, tk.getScreenSize().height / 4));

        hostLabel.setText(sshServer.getIp());

        reloadProfileBox();

        profileBox.addItemListener(e -> {
            updateProfileInfo((UploadProfile) e.getItem());
        });

        cancelBtn.addActionListener(e -> close(CANCEL_EXIT_CODE));

        uploadBtn.addActionListener(e -> {
            UploadProfile profile = (UploadProfile) profileBox.getSelectedItem();
            profile.setSelected(true);
            String filePath = profile.getFile();
            String location = profile.getLocation();

            SshStatus status = sshService.scpPut(sshServer, filePath, location);
            if (!status.isSuccess()) {
                Messages.showErrorDialog(status.getMessage(), "Upload");
                return;
            }
            if (profile.getCommandId() == null) {
                Messages.showInfoMessage("Upload success", "Upload");
                close(OK_EXIT_CODE);
                return;
            }
            Command cmd = ConfigHelper.getCommandById(profile.getCommandId());
            CommandUtil.executeAndShowMessages(project, cmd, sshServer, this);
            ApplicationContext.getApplicationContext().publishEvent(new UploadProfileSelectedEvent(profile));
        });

        actionBtn.setText("Action ▼");
        actionBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JPopupMenu menu = new JPopupMenu();
                menu.add(new JMenuItem(new AbstractAction("Add") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new AddUploadProfile(sshServer.getId(), null).showAndGet();
                    }
                }));
                menu.add(new JMenuItem(new AbstractAction("Edit") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        UploadProfile profile = (UploadProfile) profileBox.getSelectedItem();
                        new AddUploadProfile(sshServer.getId(), profile).showAndGet();
                    }
                }));
                menu.add(new JMenuItem(new AbstractAction("Remove") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        UploadProfile profile = (UploadProfile) profileBox.getSelectedItem();
                        ConfigHelper.removeUploadProfile(profile);
                        reloadProfileBox();
                    }
                }));
                menu.show(actionBtn, 0, actionBtn.getHeight());
            }
        });
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel dialog = new JPanel(new BorderLayout());
        dialog.add(mainPanel, BorderLayout.CENTER);
        return dialog;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{};
    }

    private void updateProfileInfo(UploadProfile profile) {
        fileLabel.setText(profile.getFile());
        locationLabel.setText(profile.getLocation());
        if (profile.getCommandId() != null) {
            Command cmd = ConfigHelper.getCommandById(profile.getCommandId());
            commandLabel.setText(cmd.toString());
        } else {
            commandLabel.setText("");
        }
    }

    private void reloadProfileBox() {
        profileBox.removeAllItems();
        List<UploadProfile> profiles = ConfigHelper.getUploadProfileBySshId(sshServer.getId());
        if (profiles.size() > 0) {
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
            fileLabel.setText("");
            locationLabel.setText("");
            commandLabel.setText("");
        }
    }

    @Override
    public void onApplicationEvent(UploadProfileAddEvent event) {
        reloadProfileBox();
    }
}