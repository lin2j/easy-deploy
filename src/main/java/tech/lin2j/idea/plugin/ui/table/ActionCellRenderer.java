package tech.lin2j.idea.plugin.ui.table;

import com.intellij.openapi.project.Project;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import java.util.Objects;

/**
 * @author linjinjia
 * @date 2024/5/5 17:00
 */
public class ActionCellRenderer extends DefaultTableCellRenderer {

    private final Project project;

    public ActionCellRenderer(Project project) {
        this.project = project;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        int sshId = Integer.parseInt(Objects.toString(value));
        TableActionPane actionPane = new TableActionPane(sshId, project);
        actionPane.setForeground(c.getForeground());
        actionPane.setBackground(c.getBackground());
        return actionPane;
    }

}