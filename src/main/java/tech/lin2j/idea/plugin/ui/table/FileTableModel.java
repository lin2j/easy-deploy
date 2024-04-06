package tech.lin2j.idea.plugin.ui.table;

import com.intellij.util.ui.ItemRemovable;
import tech.lin2j.idea.plugin.file.TableFile;

import javax.swing.Icon;
import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * see: com.intellij.ide.todo.configurable.PatternsTableModel
 *
 * @author linjinjia
 * @date 2024/4/6 10:07
 */
public class FileTableModel extends AbstractTableModel implements ItemRemovable {
    private final String[] ourColumnNames = new String[]{
            "Name", "Size", "Type", "Modified"
    };
    private final Class<?>[] ourColumnClasses = new Class[]{
            Icon.class, TableFile.class, String.class, String.class, String.class
    };

    private final List<? extends TableFile> myPatterns;

    public FileTableModel(List<? extends TableFile> patterns) {
        myPatterns = patterns;
    }

    @Override
    public String getColumnName(int column) {
        return ourColumnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return ourColumnClasses[column];
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public int getRowCount() {
        return myPatterns.size();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    @Override
    public Object getValueAt(int row, int column) {
        TableFile pattern = myPatterns.get(row);
        switch (column) {
            case 0: { // "Name" column
                return pattern;
            }
            case 1: { // "Size" column
                return pattern.getSize();
            }
            case 2: { // "Type" column
                return pattern.getType();
            }
            case 3: { // "Modified" column
                return pattern.getModified();
            }
            default: {
                throw new IllegalArgumentException();
            }
        }
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        // ignored when table is not editable
    }

    @Override
    public void removeRow(int index) {
        myPatterns.remove(index);
        fireTableRowsDeleted(index, index);
    }
}
