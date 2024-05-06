package tech.lin2j.idea.plugin.ui.table;

import com.intellij.openapi.project.Project;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;
import java.util.EventObject;
import java.util.Objects;

/**
 * @author linjinjia
 * @date 2024/5/5 17:00
 */
public class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final Project project;

    private TableActionPane actionPane;

    public ActionCellEditor(Project project) {
        this.project = project;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        int sshId = Integer.parseInt(Objects.toString(value));
        actionPane = new TableActionPane(sshId, project);
        return actionPane;
    }

    @Override
    public Object getCellEditorValue() {
        return actionPane.getState();
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }
}