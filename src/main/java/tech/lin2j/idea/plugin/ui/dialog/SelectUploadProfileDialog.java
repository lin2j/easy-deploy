package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.UploadProfile;
import tech.lin2j.idea.plugin.ssh.SshServer;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.util.List;

/**
 * @author linjinjia
 * @date 2024/5/1 22:46
 */
public class SelectUploadProfileDialog extends DialogWrapper {

    private final JPanel root;
    private JBList<UploadProfile> profileList;
    private ComboBox<SshServer> sshServerComboBox;

    private final StringBuilder selectedProfile;

    public SelectUploadProfileDialog(StringBuilder selectedProfile) {
        super(null);
        this.selectedProfile = selectedProfile;
        initSeverComboBox();
        initUploadProfileJBList();
        root = FormBuilder.createFormBuilder()
                .addLabeledComponent("Server", sshServerComboBox)
                .addLabeledComponent("Upload Profile", profileList, true)
                .getPanel();
        root.setPreferredSize(new Dimension(500, 0));
        init();
    }

    @Override
    protected void doOKAction() {
        SshServer server = (SshServer) sshServerComboBox.getSelectedItem();
        if (server != null) {
            int sshId = server.getId();
            UploadProfile profile = profileList.getSelectedValue();
            if (profile != null && profile.getId() != null) {
                int profileId = profile.getId();
                selectedProfile.append(sshId)
                        .append("@")
                        .append(profileId);
            }
        }

        super.doOKAction();
    }

    private void initSeverComboBox() {
        List<SshServer> sshServers = ConfigHelper.sshServers();
        sshServerComboBox = new ComboBox<>();
        sshServerComboBox.setModel(new CollectionComboBoxModel<>(sshServers));
        sshServerComboBox.addActionListener(e -> {
            setProfiles();
        });
    }

    private void initUploadProfileJBList() {
        profileList = new JBList<>();
        setProfiles();
    }

    private void setProfiles() {
        SshServer server = (SshServer) sshServerComboBox.getSelectedItem();
        if (server == null) {
            return;
        }
        int sshId = server.getId();
        List<UploadProfile> profiles = ConfigHelper.getUploadProfileBySshId(sshId);
        profileList.setModel(new CollectionListModel<>(profiles));
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return root;
    }
}