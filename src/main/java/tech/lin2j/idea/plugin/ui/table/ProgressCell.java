package tech.lin2j.idea.plugin.ui.table;

import com.intellij.openapi.progress.util.ColorProgressBar;
import com.intellij.ui.table.JBTable;
import tech.lin2j.idea.plugin.file.DirectoryInfo;
import tech.lin2j.idea.plugin.ui.ftp.FileTableContainer;

/**
 * @author linjinjia
 * @date 2024/4/15 22:23
 */
public class ProgressCell {
    private JBTable table;
    private int row;
    private ColorProgressBar colorProgressBar;
    private boolean isDirectoryRow;
    private DirectoryInfo directoryInfo;
    private long transferred;
    private String cellKey;
    private final FileTableContainer targetContainer;

    public ProgressCell(JBTable table, int row, ColorProgressBar colorProgressBar, FileTableContainer targetContainer) {
        this.table = table;
        this.row = row;
        this.colorProgressBar = colorProgressBar;
        this.targetContainer = targetContainer;
    }

    public JBTable getTable() {
        return table;
    }

    public void setTable(JBTable table) {
        this.table = table;
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

    public void setColorProgressBar(ColorProgressBar colorProgressBar) {
        this.colorProgressBar = colorProgressBar;
    }

    public boolean isDirectoryRow() {
        return isDirectoryRow;
    }

    public void setDirectoryRow(boolean directoryRow) {
        isDirectoryRow = directoryRow;
    }

    public DirectoryInfo getDirectoryInfo() {
        return directoryInfo;
    }

    public void setDirectoryInfo(DirectoryInfo directoryInfo) {
        this.directoryInfo = directoryInfo;
    }

    public long getTransferred() {
        return transferred;
    }

    public void setTransferred(long transferred) {
        this.transferred = transferred;
    }

    public void addTransferred(long transferred) {
        this.transferred += transferred;
    }

    public String getCellKey() {
        return cellKey;
    }

    public void setCellKey(String cellKey) {
        this.cellKey = cellKey;
    }

    public FileTableContainer getTargetContainer() {
        return targetContainer;
    }
}