package tech.lin2j.idea.plugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.table.JBTable;
import org.apache.commons.collections.CollectionUtils;
import org.jdesktop.swingx.prompt.PromptSupport;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.event.TableRefreshEvent;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.event.ApplicationListener;
import tech.lin2j.idea.plugin.ssh.SshServer;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.util.ArrayList;
import java.util.List;

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
    private JTextField searchInput;
    private JButton searchButton;
    String[] columnNames = {"ID", "Address", "Username", "Description", "Actions"};

    private final Project project;

    public ConsoleUi(Project project) {
        this.project = project;
        initUi();
        loadTableData(null);
    }

    private void initUi() {
        addHostBtn.addActionListener(e -> {
            // 显示 HostUi
            new HostUi(project, null).showAndGet();
        });

        refreshBtn.addActionListener(e -> {
            loadTableData(null);
        });


        PromptSupport.setPrompt("IP | Name | Desc", searchInput);
        searchInput.requestFocus(false);
        searchButton.addActionListener(e -> {
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
                String desc = server.getDescription();
                if (StringUtil.isNotEmpty(desc) && desc.contains(keyword)) {
                    searchResult.add(server);
                }
            }

            ApplicationContext.getApplicationContext().publishEvent(new TableRefreshEvent(searchResult));
        });

    }

    public JPanel getMainPanel() {
        return mainPanel;
    }


    public void loadTableData(TableRefreshEvent e) {
        searchInput.setText("");
        List<SshServer> sshServers = ConfigHelper.sshServers();
        if (e != null && e.getSshServers() != null) {
            sshServers = e.getSshServers();
        }
        Object[][] data = new Object[sshServers.size()][4];
        for (int i = 0; i < sshServers.size(); i++) {
            SshServer sshServer = sshServers.get(i);
            data[i][0] = sshServer.getId();
            data[i][1] = sshServer.getIp() + ":" + sshServer.getPort();
            data[i][2] = sshServer.getUsername();
            data[i][3] = sshServer.getDescription();
        }
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
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

    @Override
    public void onApplicationEvent(TableRefreshEvent event) {
        loadTableData(event);
    }
}