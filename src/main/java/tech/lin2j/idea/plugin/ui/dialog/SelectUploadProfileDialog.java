package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.action.ServerSearchKeyAdapter;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.UploadProfile;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ui.render.UploadProfileColoredListCellRenderer;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

/**
 * @author linjinjia
 * @date 2024/5/1 22:46
 */
public class SelectUploadProfileDialog extends DialogWrapper {

    private JPanel root;
    private JBList<UploadProfile> profileList;

    private SimpleToolWindowPanel serverContainer;
    private JBList<SshServer> serverJBList;

    private final StringBuilder selectedProfile;

    public SelectUploadProfileDialog(StringBuilder selectedProfile, Project project) {
        super(project);
        this.selectedProfile = selectedProfile;

        initServerContainer();
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
        SshServer server = serverJBList.getSelectedValue();
        if (server != null) {
            int sshId = server.getId();
            UploadProfile profile = profileList.getSelectedValue();
            if (profile != null && profile.getId() != null) {
                int profileId = profile.getId();
                selectedProfile.append(sshId).append("@").append(profileId);
            }
        }

        super.doOKAction();
    }

    private void initRoot() {
        String serverTitle = MessagesBundle.getText("dialog.profile.select.server");
        String profileTitle = MessagesBundle.getText("dialog.profile.select.profiles");
        JPanel profilePanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(profileTitle, new JBScrollPane(profileList), true)
                .getPanel();

        JPanel serverPanel = new JPanel(new GridBagLayout());
        serverPanel.add(new JBLabel(serverTitle), new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));
        serverPanel.add(serverContainer, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                JBUI.insets(0, 1), 0, 0));

        JBSplitter splitter = new JBSplitter(false, 0.4f);
        splitter.setFirstComponent(serverPanel);
        splitter.setSecondComponent(profilePanel);

        root = new JPanel(new BorderLayout());
        root.setPreferredSize(new Dimension(500, 400));
        root.add(splitter, BorderLayout.CENTER);
    }

    private void initServerContainer() {
        serverContainer = new SimpleToolWindowPanel(true);
        serverContainer.setBorder(JBUI.Borders.empty(2, 0));

        SearchTextField searchInput = new SearchTextField() {
            @Override
            protected void onFieldCleared() {
                loadServers(ConfigHelper.sshServers());
            }
        };
        searchInput.addKeyboardListener(new ServerSearchKeyAdapter(searchInput, this::loadServers));

        serverJBList = new JBList<>();
        serverJBList.addListSelectionListener(e -> setProfiles());
        loadServers(ConfigHelper.sshServers());

        serverContainer.setToolbar(searchInput);
        serverContainer.setContent(new JBScrollPane(serverJBList));
    }

    private void loadServers(List<SshServer> servers) {
        serverJBList.setModel(new CollectionComboBoxModel<>(servers));
    }

    private void initUploadProfileJBList() {
        profileList = new JBList<>();
        profileList.setCellRenderer(new UploadProfileColoredListCellRenderer());
        setProfiles();
    }

    private void setProfiles() {
        SshServer server = serverJBList.getSelectedValue();
        if (server == null) {
            if (serverJBList.getItemsCount() == 0) {
                return;
            }
            serverJBList.setSelectedIndex(0);
            server = serverJBList.getSelectedValue();
        }
        int sshId = server.getId();
        List<UploadProfile> profiles = ConfigHelper.getUploadProfileBySshId(sshId);
        profileList.setModel(new CollectionListModel<>(profiles));
    }
}