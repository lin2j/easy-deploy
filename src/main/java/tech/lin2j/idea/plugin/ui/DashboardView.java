package tech.lin2j.idea.plugin.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import icons.MyIcons;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.action.GithubAction;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.event.TableRefreshEvent;
import tech.lin2j.idea.plugin.event.ApplicationListener;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ui.dialog.SettingsDialog;
import tech.lin2j.idea.plugin.uitl.WebBrowseUtil;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author linjinjia
 * @date 2024/4/25 06:45
 */
public class DashboardView extends SimpleToolWindowPanel implements ApplicationListener<TableRefreshEvent> {
    private static final String[] COLUMNS = {"ID", "Address", "Username", "Tag", "Description", "Actions"};

    private JBTable hostTable;
    private ComboBox<String> tagComboBox;
    private SearchTextField searchInput;
    private final Project project;

    public DashboardView(Project project) {
        super(true);
        this.project = project;

        initToolbar();
        initHostTable();
    }

    @Override
    public void onApplicationEvent(TableRefreshEvent event) {
        if (event.isTagRefresh()) {
            refreshTagComboBox();
            return;
        }
        loadTableData(event);
    }

    private void initToolbar() {
        initTagComboBox();
        initSearchTextField();

        final JPanel northPanel = new JPanel(new GridBagLayout());

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new GithubAction());
        actionGroup.addSeparator();
        actionGroup.add(new SettingsAction());
        actionGroup.add(new RefreshAction());
        actionGroup.add(new AddHostAction());
        ActionToolbar toolbar = ActionManager.getInstance()
                .createActionToolbar("DashboardView@Toolbar", actionGroup, true);
        toolbar.setTargetComponent(this);

        northPanel.setBorder(JBUI.Borders.empty(2, 0));
        northPanel.add(tagComboBox, new GridBagConstraints(0, 0, 1, 1, 0, 1, GridBagConstraints.WEST, GridBagConstraints.NONE,
                JBUI.emptyInsets(), 0, 0));
        northPanel.add(searchInput, new GridBagConstraints(1, 0, 1, 1, 0, 1, GridBagConstraints.WEST, GridBagConstraints.NONE,
                JBUI.emptyInsets(), 0, 0));
        northPanel.add(toolbar.getComponent(), new GridBagConstraints(2, 0, 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE,
                JBUI.emptyInsets(), 0, 0));

        setToolbar(northPanel);
    }

    private void initTagComboBox() {
        tagComboBox = new ComboBox<>();
        tagComboBox.addItemListener(e -> {
            String tag = Objects.toString(e.getItem());
            if (StringUtil.isEmpty(tag)) {
                loadTableData(null);
                return;
            }
            List<SshServer> servers = ConfigHelper.sshServers();
            servers = servers.stream().filter(s -> Objects.equals(tag, s.getTag())).collect(Collectors.toList());
            loadTableData(new TableRefreshEvent(servers));
        });

        refreshTagComboBox();
    }

    private void refreshTagComboBox() {
        List<String> tags = new ArrayList<>();
        tags.add("");
        tags.addAll(ConfigHelper.getServerTags());
        tagComboBox.setModel(new CollectionComboBoxModel<>(tags));
    }

    private void initSearchTextField() {
        searchInput = new SearchTextField();
        searchInput.addKeyboardListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                search(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                search(e);
            }

            private void search(KeyEvent e) {
                String keyword = searchInput.getText();
                if (StringUtil.isEmpty(keyword)) {
                    return;
                }

                List<SshServer> searchResult = new ArrayList<>();
                List<SshServer> serverInConfig = ConfigHelper.sshServers();
                if (CollectionUtils.isEmpty(serverInConfig)) {
                    return;
                }

                for (SshServer server : serverInConfig) {
                    if (server.getIp().contains(keyword)) {
                        searchResult.add(server);
                        continue;
                    }
                    if (server.getUsername().contains(keyword)) {
                        searchResult.add(server);
                        continue;
                    }
                    if (server.getTag() != null && server.getTag().contains(keyword)) {
                        searchResult.add(server);
                        continue;
                    }
                    String desc = server.getDescription();
                    if (StringUtil.isNotEmpty(desc) && desc.contains(keyword)) {
                        searchResult.add(server);
                    }
                }

                loadTableData(new TableRefreshEvent(searchResult));

            }
        });
    }

    private void initHostTable() {
        hostTable = new JBTable();


        setContent(new JScrollPane(hostTable));
        loadTableData(null);
    }

    public void loadTableData(TableRefreshEvent e) {
        List<SshServer> sshServers = ConfigHelper.sshServers();
        if (e != null && e.getSshServers() != null) {
            sshServers = e.getSshServers();
        }
        Object[][] data = new Object[sshServers.size()][5];
        for (int i = 0; i < sshServers.size(); i++) {
            SshServer sshServer = sshServers.get(i);
            data[i][0] = sshServer.getId();
            data[i][1] = sshServer.getIp() + ":" + sshServer.getPort();
            data[i][2] = sshServer.getUsername();
            data[i][3] = sshServer.getTag();
            data[i][4] = sshServer.getDescription();
        }
        DefaultTableModel tableModel = new DefaultTableModel(data, COLUMNS) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        hostTable.setModel(tableModel);
        hostTable.getEmptyText().setText("No Data");
        // hide ID column
        hostTable.removeColumn(hostTable.getColumn("ID"));

        TableActionUi tableActionUi = new TableActionUi(project);
        TableColumn actionColumn = hostTable.getColumn("Actions");
        actionColumn.setCellRenderer(tableActionUi);
        actionColumn.setCellEditor(tableActionUi);
        actionColumn.setMinWidth(450);
        actionColumn.setMaxWidth(550);
    }

    private class RefreshAction extends AnAction {

        public RefreshAction() {
            super("Refresh", "Refresh host table", MyIcons.Actions.Refresh);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            refreshTagComboBox();
            loadTableData(null);
        }
    }

    private class AddHostAction extends AnAction {
        public AddHostAction() {
            super("Add Host", "Add new host profile", MyIcons.Actions.AddHost);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            new HostUi(project, null).showAndGet();
        }
    }

    private class SettingsAction extends AnAction {
        public SettingsAction() {
            super("Plugins Settings", "Update plugin settings", MyIcons.Actions.Settings);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            SettingsDialog.show(project);
        }
    }
}