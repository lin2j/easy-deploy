package tech.lin2j.idea.plugin.ui.table;

import com.intellij.openapi.progress.util.ColorProgressBar;
import com.intellij.ui.table.JBTable;
import tech.lin2j.idea.plugin.file.DirectoryInfo;

import javax.swing.table.TableModel;

/**
 * @author linjinjia
 * @date 2024/4/15 22:23
 */
public class ProgressCell {
    private final JBTable table;
    private final ColorProgressBar colorProgressBar;
    private DirectoryInfo directoryInfo;
    private int row;
    private long transferred;

    public ProgressCell(JBTable table, int row, ColorProgressBar colorProgressBar) {
        this.table = table;
        this.row = row;
        this.colorProgressBar = colorProgressBar;
    }

    public JBTable getTable() {
        return table;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public ColorProgressBar getColorProgressBar() {
        return colorProgressBar;
    }

    public boolean isDirectoryRow() {
        return directoryInfo.isDirectory();
    }

    public long getDirectorySize() {
        return directoryInfo.getSize();
    }

    public TableModel getTableModel() {
        return table.getModel();
    }

    public void setDirectoryInfo(DirectoryInfo directoryInfo) {
        this.directoryInfo = directoryInfo;
    }

    public long getTransferred() {
        return transferred;
    }

    public void addTransferred(long transferred) {
        this.transferred += transferred;
    }
}