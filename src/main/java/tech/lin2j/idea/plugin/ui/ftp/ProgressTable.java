package tech.lin2j.idea.plugin.ui.ftp;

import com.intellij.openapi.progress.util.ColorProgressBar;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.table.JBTable;
import tech.lin2j.idea.plugin.domain.model.event.FileTransferEvent;
import tech.lin2j.idea.plugin.event.ApplicationListener;
import tech.lin2j.idea.plugin.file.TableFile;
import tech.lin2j.idea.plugin.file.TransferRelationship;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author linjinjia
 * @date 2024/4/4 12:19
 */
public class ProgressTable extends JPanel implements ApplicationListener<FileTransferEvent> {

    private static final int NAME = 0;
    private static final int STATE = 1;
    private static final int PROGRESS = 2;
    private static final int SIZE = 3;
    private static final int LOCAl = 4;
    private static final int REMOTE = 5;

    private JBTable outputTable;
    private DefaultTableModel tableModel;
    private final FileTableContainer localContainer;
    private final FileTableContainer remoteContainer;
    private final String[] columnNames = {"Name", "State", "Progress", "Size", "Local", "Remote"};

    public ProgressTable(FileTableContainer localContainer, FileTableContainer remoteContainer) {
        this.localContainer = localContainer;
        this.remoteContainer = remoteContainer;

        setLayout(new BorderLayout());
        init();
    }

    @Override
    public void onApplicationEvent(FileTransferEvent event) {
        if (event.getState().isEnd()) {
            cleanTable();
            return;
        }
        boolean isUpload = event.isUpload();

        FileTableContainer sourceContainer = isUpload ? localContainer : remoteContainer;
        List<TableFile> source = sourceContainer.getSelectedFiles();
        String target = isUpload ? remoteContainer.getPath() : localContainer.getPath();

        for (TableFile tf : source) {
            String name = tf.getName();
            String state = isUpload ? "Uploading" : "Downloading";
            String size = tf.getSize();
            String local = tf.getFilePath();
            String remote = target + "/" + name;

            Object[] data;

            if (isUpload) {
                data = new Object[] {name, state, tf, size, local, remote};
            } else {
                data = new Object[] {name, state, tf, size, remote, local};
            }

            tableModel.addRow(data);
        }
    }

    private void cleanTable() {
        tableModel.setRowCount(0);
    }

    private void init() {
        outputTable = new JBTable();
        Object[][] data = new Object[0][6];

        tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        outputTable.setModel(tableModel);
        outputTable.setFocusable(false);
        outputTable.setRowHeight(30);

        TableColumn progressColum = outputTable.getColumnModel().getColumn(PROGRESS);
        progressColum.setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            ColorProgressBar progressBar = new ColorProgressBar();
            progressBar.setSize(140, 25);
            progressBar.setFraction(0.5);
            return progressBar;
        });

        progressColum.setMinWidth(150);
        progressColum.setMaxWidth(150);

        TableColumn nameColumn = outputTable.getColumnModel().getColumn(NAME);
        nameColumn.setMaxWidth(500);
        nameColumn.setMinWidth(300);

        TableColumn stateColumn = outputTable.getColumnModel().getColumn(STATE);
        stateColumn.setMaxWidth(150);
        stateColumn.setMinWidth(150);

        TableColumn sizeColumn = outputTable.getColumnModel().getColumn(SIZE);
        sizeColumn.setMaxWidth(150);
        sizeColumn.setMinWidth(150);

        add(new JScrollPane(outputTable));
    }
}