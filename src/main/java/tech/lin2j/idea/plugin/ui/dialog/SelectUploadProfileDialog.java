package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.UploadProfile;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ui.render.UploadProfileColoredListCellRenderer;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;
import tech.lin2j.idea.plugin.uitl.UiUtil;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

/**
 * @author linjinjia
 * @date 2024/5/1 22:46
 */
public class SelectUploadProfileDialog extends DialogWrapper {

    private JPanel root;
    private JBList<UploadProfile> profileList;
    private JBList<SshServer> serverList;

    private final StringBuilder selectedProfile;

    public SelectUploadProfileDialog(StringBuilder selectedProfile, Project project) {
        super(project);
        this.selectedProfile = selectedProfile;

        initServerList();
        initUploadProfileJBList();
        initRoot();

        setTitle(MessagesBundle.getText("dialog.profile.select.frame"));
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return root;
    }

    @Override
    protected void doOKAction() {
        SshServer server = serverList.getSelectedValue();
        UploadProfile profile = profileList.getSelectedValue();

        if (server != null && profile != null && profile.getId() != null) {
            int sshId = server.getId();
            int profileId = profile.getId();
            selectedProfile.append(sshId).append("@").append(profileId);
        }

        super.doOKAction();
    }

    private void initRoot() {
        String serverTitle = "Server:";
        String profileTitle = MessagesBundle.getText("dialog.profile.select.profiles");

        JPanel serverPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(serverTitle, new JBScrollPane(serverList), true)
                .getPanel();

        JPanel profilePanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(profileTitle, new JBScrollPane(profileList), true)
                .getPanel();

        root = new JPanel(new GridBagLayout());
        root.setPreferredSize(new Dimension(UiUtil.screenWidth() / 2, 400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        root.add(serverPanel, gbc);

        gbc.gridx = 1;
        root.add(profilePanel, gbc);
    }

    private void initServerList() {
        serverList = new JBList<>();
        serverList.setModel(new CollectionComboBoxModel<>(ConfigHelper.sshServers()));
    }

    private void initUploadProfileJBList() {
        profileList = new JBList<>();
        profileList.setCellRenderer(new UploadProfileColoredListCellRenderer());
        loadProfiles();
    }

    private void loadProfiles() {
        var profiles = ConfigHelper.getAllUploadProfiles();
        profileList.setModel(new CollectionComboBoxModel<>(profiles));
    }
}
