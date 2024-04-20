package tech.lin2j.idea.plugin.ui.ftp;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.util.ColorProgressBar;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.table.JBTable;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.xfer.TransferListener;
import tech.lin2j.idea.plugin.domain.model.event.FileTransferEvent;
import tech.lin2j.idea.plugin.enums.TransferCellState;
import tech.lin2j.idea.plugin.event.ApplicationListener;
import tech.lin2j.idea.plugin.file.DirectoryInfo;
import tech.lin2j.idea.plugin.file.ProgressTransferListener;
import tech.lin2j.idea.plugin.file.TableFile;
import tech.lin2j.idea.plugin.ui.table.ProgressCell;
import tech.lin2j.idea.plugin.uitl.FTPUtil;
import tech.lin2j.idea.plugin.uitl.FileUtil;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static tech.lin2j.idea.plugin.enums.TransferCellState.DOWNLOADING;
import static tech.lin2j.idea.plugin.enums.TransferCellState.UPLOADING;

/**
 * @author linjinjia
 * @date 2024/4/4 12:19
 */
public class ProgressTable extends JPanel implements ApplicationListener<FileTransferEvent> {
    private static final Logger log = Logger.getInstance(ProgressTable.class);

    public static final int NAME_COL = 0;
    public static final int STATE_COL = 1;
    public static final int PROGRESS_COL = 2;
    public static final int SIZE_COL = 3;
    public static final int LOCAl_COL = 4;
    public static final int REMOTE_COL = 5;

    private JBTable outputTable;
    private DefaultTableModel tableModel;
    private final FileTableContainer localContainer;
    private final FileTableContainer remoteContainer;
    private final SFTPClient sftpClient;
    private final String[] columnNames = {"Name", "State", "Progress", "Size", "Local", "Remote"};
    private int rows = 0;
    private final BlockingQueue<TransferTask> TASK_QUEUE = new ArrayBlockingQueue<>(1000);
    private final Thread transferTaskThread = new Thread(() -> {
        for (; ; ) {
            try {
                TransferTask task = TASK_QUEUE.take();
                task.run();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    });

    public ProgressTable(FileTableContainer localContainer, FileTableContainer remoteContainer) {
        this.localContainer = localContainer;
        this.remoteContainer = remoteContainer;
        sftpClient = this.remoteContainer.getFTPClient();

        setLayout(new BorderLayout());
        init();

        transferTaskThread.start();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        transferTaskThread.interrupt();
    }

    @Override
    public void onApplicationEvent(FileTransferEvent event) {
        if (event.getState().isEnd()) {
            cleanTable();
            return;
        }
        boolean isUpload = event.isUpload();

        FileTableContainer sourceContainer = isUpload ? localContainer : remoteContainer;
        FileTableContainer targetContainer = isUpload ? remoteContainer : localContainer;
        List<TableFile> source = sourceContainer.getSelectedFiles();
        String sourcePath = sourceContainer.getPath();

        try {
            for (TableFile tf : source) {
                boolean isDirectory = tf.isDirectory();
                String name = tf.getName();
                TransferCellState state;
                String size;
                String local;
                String remote;
                Object[] data;

                ColorProgressBar progressBar = new ColorProgressBar();
                ProgressCell cell = new ProgressCell(outputTable, rows, progressBar, targetContainer);
                cell.setDirectoryRow(isDirectory);
                if (isUpload) {
                    local = tf.getFilePath();
                    remote = remoteContainer.getPath() + "/" + tf.getName();

                    state = UPLOADING;
                    DirectoryInfo di = FileUtil.calculateDirectorySize(local);
                    di.setUpload(true);
                    cell.setDirectoryInfo(di);
                    cell.setCellKey(local);
                    size = StringUtil.formatFileSize(di.getSize());
                } else {
                    local = localContainer.getPath() + "/" + tf.getName();
                    remote = tf.getFilePath();

                    progressBar.setColor(ColorProgressBar.GREEN);
                    state = DOWNLOADING;
                    DirectoryInfo di = FTPUtil.calculateRemoteDirectorySize(sftpClient, remote);
                    di.setUpload(false);
                    cell.setDirectoryInfo(di);
                    cell.setCellKey(remote);
                    size = StringUtil.formatFileSize(di.getSize());

                }
                data = new Object[]{name, state, progressBar, size, local, remote};

                tableModel.addRow(data);
                rows++;

                // serialize transfer task
                TransferListener transferListener = new ProgressTransferListener(sourcePath, cell);
                TASK_QUEUE.add(new TransferTask(sftpClient, transferListener, isUpload, local, remote));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void cleanTable() {
        rows = 0;
        tableModel.setRowCount(0);
    }

    private void init() {
        outputTable = new JBTable();
        tableModel = new DefaultTableModel(new Object[0][6], columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        outputTable.setModel(tableModel);
        outputTable.setFocusable(false);
        outputTable.setRowHeight(30);

        TableColumn progressColum = outputTable.getColumnModel().getColumn(PROGRESS_COL);
        progressColum.setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            ColorProgressBar progressBar = (ColorProgressBar) value;
            progressBar.setPreferredSize(new Dimension(140, 16));
            return progressBar;
        });

        progressColum.setMinWidth(150);
        progressColum.setMaxWidth(150);

        TableColumn nameColumn = outputTable.getColumnModel().getColumn(NAME_COL);
        nameColumn.setMaxWidth(500);
        nameColumn.setMinWidth(300);

        TableColumn stateColumn = outputTable.getColumnModel().getColumn(STATE_COL);
        stateColumn.setMaxWidth(150);
        stateColumn.setMinWidth(150);

        TableColumn sizeColumn = outputTable.getColumnModel().getColumn(SIZE_COL);
        sizeColumn.setMaxWidth(150);
        sizeColumn.setMinWidth(150);

        add(new JScrollPane(outputTable));
    }

    private static class TransferTask {
        private final SFTPClient sftpClient;
        private final TransferListener transferListener;
        private final boolean isUpload;
        private final String local;
        private final String remote;

        public TransferTask(SFTPClient sftpClient, TransferListener transferListener,
                            boolean isUpload, String local, String remote) {
            this.sftpClient = sftpClient;
            this.transferListener = transferListener;
            this.isUpload = isUpload;
            this.local = local;
            this.remote = remote;
        }

        public void run() throws IOException {
            sftpClient.getFileTransfer().setTransferListener(transferListener);
            if (isUpload) {
                sftpClient.getFileTransfer().upload(local, remote);
            } else {
                sftpClient.getFileTransfer().download(remote, local);
            }
        }
    }
}