package tech.lin2j.idea.plugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.table.JBTable;
import org.apache.commons.collections.CollectionUtils;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.event.TableRefreshEvent;
import tech.lin2j.idea.plugin.event.ApplicationListener;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ui.dialog.SettingsDialog;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author linjinjia
 * @date 2022/4/24 16:20
 */
public class ConsoleUi implements ApplicationListener<TableRefreshEvent> {
    private JPanel mainPanel;
    private JBTable hostTable;
    private JPanel optPanel;
    private JButton addHostBtn;
    private JButton refreshBtn;
    private JButton settingsButton;
    private ComboBox<String> tagComboBox;
    private SearchTextField searchInput;
    private final String[] columnNames = {"ID", "Address", "Username", "Tag", "Description", "Actions"};

    private final Project project;

    public ConsoleUi(Project project) {
        this.project = project;
        initUi();
        loadTableData(null);
    }

    private void initUi() {
        settingsButton.addActionListener(e -> SettingsDialog.show(project));

        addHostBtn.addActionListener(e -> {
            // 显示 HostUi
            new HostUi(project, null).showAndGet();
        });

        refreshBtn.addActionListener(e -> {
            refreshTagComboBox();
            loadTableData(null);
        });

        initTagComboBox();

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

    private void initTagComboBox() {
        refreshTagComboBox();
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
    }

    public JPanel getMainPanel() {
        return mainPanel;
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
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };
        hostTable.setModel(tableModel);
        hostTable.setFocusable(false);
        hostTable.setRowSelectionAllowed(false);
        hostTable.setFillsViewportHeight(true);
        hostTable.setRowHeight(30);
        hostTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // hide ID column
        hostTable.removeColumn(hostTable.getColumn("ID"));

        TableActionUi tableActionUi = new TableActionUi(project);
        TableColumn actionColumn = hostTable.getColumn("Actions");
        actionColumn.setCellRenderer(tableActionUi);
        actionColumn.setCellEditor(tableActionUi);
        actionColumn.setMinWidth(450);
        actionColumn.setMaxWidth(550);
    }

    private void refreshTagComboBox() {
        List<String> tags = new ArrayList<>();
        tags.add("");
        tags.addAll(ConfigHelper.getServerTags());
        tagComboBox.setModel(new CollectionComboBoxModel<>(tags));
    }

    @Override
    public void onApplicationEvent(TableRefreshEvent event) {
        if (event.isTagRefresh()) {
            refreshTagComboBox();
            return;
        }
        loadTableData(event);
    }
}