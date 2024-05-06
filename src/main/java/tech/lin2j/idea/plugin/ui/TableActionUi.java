package tech.lin2j.idea.plugin.ui;


import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.lin2j.idea.plugin.action.CommandDialogAction;
import tech.lin2j.idea.plugin.action.HostMoreOpsAction;
import tech.lin2j.idea.plugin.action.OpenTerminalAction;
import tech.lin2j.idea.plugin.action.UploadDialogAction;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.util.EventObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linjinjia
 * @date 2022/4/26 10:40
 */
public class TableActionUi extends JBLabel implements TableCellRenderer, TableCellEditor {
    public static final Logger log = LoggerFactory.getLogger(TableActionUi.class);

    private JPanel actionPanel;
    private JButton uploadBtn;
    private JButton commandBtn;
    private JButton terminalBtn;
    private JButton moreBtn;

    private int sshId;
    private final Project project;

    private int verticalAlignment = CENTER;

    Map<String, TableActionUi> uiMap = new ConcurrentHashMap<>();

    public TableActionUi(Project project) {
        super();
        setName("Table.cellRenderer");

        this.project = project;

        uploadBtn.addActionListener(new UploadDialogAction(sshId, project));
        commandBtn.addActionListener(new CommandDialogAction(sshId, project));
        terminalBtn.addActionListener(new OpenTerminalAction(sshId, project));
        moreBtn.addActionListener(new HostMoreOpsAction(sshId, project, moreBtn));
    }

    @Override
    public void setVerticalAlignment(int alignment) {
        if (alignment == verticalAlignment) {
            return;
        }
        int oldValue = verticalAlignment;
        verticalAlignment = checkVerticalKey(alignment, "verticalAlignment");
        firePropertyChange("verticalAlignment", oldValue, verticalAlignment);
        repaint();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        TableActionUi actionUi = getTableActionUi(table, row, column);

        Color bg = null;
        Color fg = null;

        JTable.DropLocation dropLocation = table.getDropLocation();
        if (dropLocation != null
                && dropLocation.isInsertRow()
                && dropLocation.getRow() == row) {
            bg = UIManager.getColor("Table.dropCellBackground");
            fg = UIManager.getColor("Table.dropCellForeground");
            isSelected = true;
        }
        if (isSelected) {
            actionUi.actionPanel.setBackground(bg == null ? table.getSelectionBackground() : bg);
            actionUi.actionPanel.setForeground(fg == null ? table.getSelectionForeground() : fg);
        } else {
            actionUi.actionPanel.setBackground(table.getBackground());
            actionUi.actionPanel.setForeground(table.getForeground());
        }
        return actionUi.actionPanel;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                 int row, int column) {
        return getTableActionUi(table, row, column).actionPanel;
    }

    private TableActionUi getTableActionUi(JTable table, int row, int column) {
        String location = row + ":" + column;
        TableActionUi ui = uiMap.get(location);
        if (ui == null) {
            ui = new TableActionUi(project);
            Object sshId = table.getModel().getValueAt(row, 0);
            ui.sshId = Integer.parseInt(sshId.toString());
            uiMap.put(location, ui);
        }
        return ui;
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean stopCellEditing() {
        return true;
    }

    @Override
    public void cancelCellEditing() {

    }

    @Override
    public void addCellEditorListener(CellEditorListener l) {

    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {

    }

}