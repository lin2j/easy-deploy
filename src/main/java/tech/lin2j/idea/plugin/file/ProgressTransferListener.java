package tech.lin2j.idea.plugin.file;

import com.google.common.util.concurrent.AtomicDouble;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.xfer.TransferListener;
import tech.lin2j.idea.plugin.ui.ftp.ProgressTable;
import tech.lin2j.idea.plugin.ui.table.ProgressCell;

import javax.swing.table.DefaultTableModel;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * @author linjinjia
 * @date 2024/4/13 16:09
 */
public class ProgressTransferListener implements TransferListener {
    private final String relPath;
    private final ConsoleView consoleView;
    private final ProgressCell progressCell;

    public ProgressTransferListener(String relPath, ProgressCell cell, ConsoleView consoleView) {
        if (!relPath.endsWith("/")) {
            relPath += "/";
        }
        this.relPath = relPath;
        this.progressCell = cell;
        this.consoleView = consoleView;
    }

    @Override
    public TransferListener directory(String name) {
        return new ProgressTransferListener(relPath + name + "/", progressCell, consoleView);
    }

    @Override
    public StreamCopier.Listener file(final String name, final long size) {
        final String path = relPath + name;
        print("Transfer file: " + path + "\n");
        AtomicDouble prePercent = new AtomicDouble(0);
        AtomicLong preTransferred = new AtomicLong();
        return transferred -> {
            long finalSize = size;
            long finalTransferred = transferred;

            if (progressCell.isDirectoryRow()) {
                finalSize = progressCell.getDirectorySize();
                long len = transferred - preTransferred.get();
                progressCell.addTransferred(len);
                finalTransferred = progressCell.getTransferred();
                preTransferred.set(transferred);
            }

            double percent = 0;
            if (finalSize > 0) {
                percent = finalTransferred / (double) finalSize;
            }

            boolean completed = 1 - percent < 1e-6;
            boolean step = Math.abs(percent - prePercent.get() - 0.01) > 1e-6;
            if (step || completed) {
                DefaultTableModel tableModel = (DefaultTableModel) progressCell.getTableModel();
                int row = progressCell.getRow();

                prePercent.set(percent);
                progressCell.getColorProgressBar().setFraction(percent);
                tableModel.fireTableCellUpdated(row, ProgressTable.PROGRESS_COL);

                log((int)(percent * 100), completed);
            }

        };
    }

    private void log(int complete, boolean completed) {
        StringBuilder sb = new StringBuilder("[");
        Stream.generate(() -> '#').limit(complete).forEach(sb::append);
        Stream.generate(() -> '_').limit(100 - complete).forEach(sb::append);
        sb.append("] ");
        if (completed) {
            sb.append("complete\n");
        } else {
            sb.append(complete).append("%");
        }
        print("\r");
        print(sb.toString());
    }

    private void print(String msg) {
        consoleView.print(msg, ConsoleViewContentType.NORMAL_OUTPUT);
    }
}