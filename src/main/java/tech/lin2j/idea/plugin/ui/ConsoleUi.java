package tech.lin2j.idea.plugin.ui;

import com.intellij.openapi.project.Project;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.domain.model.event.TableRefreshEvent;
import tech.lin2j.idea.plugin.event.ApplicationListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.util.List;

/**
 * @author linjinjia
 * @date 2022/4/24 16:20
 */
public class ConsoleUi implements ApplicationListener<TableRefreshEvent> {
    private JPanel mainPanel;
    private JTable hostTable;
    private JPanel optPanel;
    private JButton addHostBtn;
    private JButton refreshBtn;
    String[] columnNames = {"ID", "Address", "UserName", "Description", "Actions"};

    private final Project project;

    public ConsoleUi(Project project) {
        this.project = project;
        initUi();
        loadTableData();
    }

    private void initUi() {
        addHostBtn.addActionListener(e -> {
            // 显示 HostUi
            new HostUi(project).showAndGet();
        });

        refreshBtn.addActionListener(e -> {
            loadTableData();
        });

    }

    public JPanel getMainPanel() {
        return mainPanel;
    }


    public void loadTableData() {
        List<SshServer> sshServers = ConfigHelper.sshServers();
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

        TableActionUi tableActionUi = new TableActionUi(project);
        TableColumn actionColumn = hostTable.getColumn("Actions");
        actionColumn.setCellRenderer(tableActionUi);
        actionColumn.setCellEditor(tableActionUi);

        hostTable.setRowHeight(30);
        hostTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // hide ID column
        hostTable.removeColumn(hostTable.getColumn("ID"));
    }

    @Override
    public void onApplicationEvent(TableRefreshEvent event) {
        loadTableData();
    }
}