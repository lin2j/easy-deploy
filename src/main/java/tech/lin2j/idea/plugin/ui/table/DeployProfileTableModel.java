package tech.lin2j.idea.plugin.ui.table;

import com.intellij.util.ui.ItemRemovable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.model.Command;
import tech.lin2j.idea.plugin.model.DeployProfile;
import tech.lin2j.idea.plugin.model.UploadProfile;
import tech.lin2j.idea.plugin.ssh.SshServer;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * @author linjinjia
 * @date 2024/6/2 21:21
 */
public class DeployProfileTableModel extends AbstractTableModel implements ItemRemovable {
    private static final String[] COLUMNS = {"Active", "Server", "Profile", "Local", "Remote", "Command"};
    private static final Class<?>[] COLUMN_CLASS = {
            Boolean.class, String.class, String.class, String.class, String.class, String.class
    };

    private final List<DeployProfile> deployProfiles;

    public DeployProfileTableModel(@NotNull List<DeployProfile> deployProfiles) {
        this.deployProfiles = deployProfiles;
    }

    @Override
    public int getRowCount() {
        return deployProfiles.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Nls
    @Override
    public String getColumnName(int columnIndex) {
        return COLUMNS[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return COLUMN_CLASS[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        DeployProfile deployProfile = this.deployProfiles.get(rowIndex);
        SshServer server = deployProfile.getServer();
        UploadProfile up = deployProfile.getUploadProfile();
        Command cmd = deployProfile.getCommand();
        switch (columnIndex) {
            case 0: { // active
                return deployProfile.isActive();
            } case 1: { // server
                return server.getIp() + ":" + server.getPort();
            } case 2: { // upload name
                return up.getName();
            } case 3: { // upload file
                return up.getFile();
            } case 4: { // upload location
                return up.getLocation();
            } case 5: { // command title
                String flag = cmd.getSharable() ? "*" : "";
                return flag + cmd.getTitle();
            } default: {
                throw new IllegalArgumentException();
            }
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        DeployProfile deployProfile = this.deployProfiles.get(rowIndex);
        if (columnIndex == 0) {
            deployProfile.setActive((Boolean) aValue);
        }
    }

    @Override
    public void addTableModelListener(TableModelListener l) {

    }

    @Override
    public void removeTableModelListener(TableModelListener l) {

    }

    @Override
    public void removeRow(int idx) {
        this.deployProfiles.remove(idx);
        fireTableRowsDeleted(idx, idx);
    }
}