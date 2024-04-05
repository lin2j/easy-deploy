package tech.lin2j.idea.plugin.ui.ftp;

import com.intellij.ui.table.JBTable;
import tech.lin2j.idea.plugin.domain.model.event.FileTransferEvent;
import tech.lin2j.idea.plugin.event.ApplicationListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;

/**
 * @author linjinjia
 * @date 2024/4/4 12:19
 */
public class ProgressTable extends JPanel implements ApplicationListener<FileTransferEvent> {

    private JBTable outputTable;
    private final String[] columnNames = {"Name", "State", "Progress", "Size", "Local", "Remote"};

    @Override
    public void onApplicationEvent(FileTransferEvent event) {

    }

    public ProgressTable() {
        setLayout(new BorderLayout());
        init();
    }

    private void init() {
        outputTable = new JBTable();
        Object[][] data = new Object[0][6];

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
        outputTable.setModel(tableModel);
        outputTable.setFocusable(false);
        outputTable.setRowSelectionAllowed(false);
        outputTable.setFillsViewportHeight(true);
        outputTable.setRowHeight(30);
        outputTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        add(new JScrollPane(outputTable));
    }
}