package tech.lin2j.idea.plugin.file;

import com.google.common.util.concurrent.AtomicDouble;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.xfer.TransferListener;
import tech.lin2j.idea.plugin.enums.TransferCellState;
import tech.lin2j.idea.plugin.ui.ftp.ProgressTable;
import tech.lin2j.idea.plugin.ui.table.ProgressCell;

import javax.swing.table.DefaultTableModel;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author linjinjia
 * @date 2024/4/13 16:09
 */
public class ProgressTransferListener implements TransferListener {
    private final String relPath;
    private final ProgressCell progressCell;

    public ProgressTransferListener(String relPath, ProgressCell cell) {
        if (!relPath.endsWith("/")) {
            relPath += "/";
        }
        this.relPath = relPath;
        this.progressCell = cell;
    }

    @Override
    public TransferListener directory(String name) {
        return new ProgressTransferListener(relPath + name + "/", progressCell);
    }

    @Override
    public StreamCopier.Listener file(final String name, final long size) {
        // transferred file
        final String path = relPath + name;
        AtomicDouble prePercent = new AtomicDouble(0);
        AtomicLong preTransferred = new AtomicLong();
        return transferred -> {
            long finalSize = size;
            long finalTransferred = transferred;

            if (progressCell.isDirectoryRow()) {
                finalSize = progressCell.getDirectoryInfo().getSize();
                long len = transferred - preTransferred.get();
                progressCell.addTransferred(len);
                finalTransferred = progressCell.getTransferred();
                preTransferred.set(transferred);
            }

            double percent = 0;
            if (finalSize > 0) {
                percent = finalTransferred / (double) finalSize;
            }

            DefaultTableModel tableModel = (DefaultTableModel) progressCell.getTable().getModel();
            int row = progressCell.getRow();
            boolean completed = 1 - percent < 1e-6;
            boolean step = Math.abs(percent - prePercent.get() - 0.02) > 1e-6;
            if (step || completed) {
                prePercent.set(percent);
                progressCell.getColorProgressBar().setFraction(percent);
                tableModel.fireTableCellUpdated(row, ProgressTable.PROGRESS_COL);
            }

            if (completed) {
                TransferCellState s = (TransferCellState) tableModel.getValueAt(row, ProgressTable.STATE_COL);
                tableModel.setValueAt(s.nextState(), row, ProgressTable.STATE_COL);
                progressCell.getTargetContainer().refreshFileList();
            }

        };
    }
}