package tech.lin2j.idea.plugin.ui;


import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.event.TableRefreshEvent;
import tech.lin2j.idea.plugin.event.ApplicationContext;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linjinjia
 * @date 2022/4/26 10:40
 */
public class TableActionUi extends JLabel implements TableCellRenderer, TableCellEditor {
    private JPanel actionPanel;
    private JButton uploadBtn;
    private JButton commandBtn;
    private JButton terminalBtn;
    private JButton removeBtn;

    private int selectedRow;
    private int selectedCol;

    private int sshId;

    private int verticalAlignment = CENTER;

    Map<String, TableActionUi> uiMap = new ConcurrentHashMap<>();

    public TableActionUi() {
        super();
        setName("Table.cellRenderer");

        uploadBtn.addActionListener(new UploadActionListener());
        commandBtn.addActionListener(new ExecuteActionListener());
        terminalBtn.addActionListener(new TerminalActionListener());
        removeBtn.addActionListener(new RemoveActionListener());
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
        TableActionUi ui = getTableActionUi(table, row, column);
        Color tableBg;
        if (isSelected) {
            tableBg = table.getSelectionBackground();
        } else {
            tableBg = table.getBackground();
        }
        ui.actionPanel.setBackground(tableBg);
        return ui.actionPanel;
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
            ui = new TableActionUi();
            ui.selectedRow = row;
            ui.selectedCol = column;
            Object sshId = table.getModel().getValueAt(row, 0);
            ui.sshId = Integer.parseInt(sshId.toString());
            uiMap.put(location, ui);
        }
        return ui;
    }

    class UploadActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            new UploadUi(ConfigHelper.getSshServerById(sshId)).showAndGet();
        }
    }

    class ExecuteActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            SelectCommandUi ui = new SelectCommandUi(sshId);
            ApplicationContext.getApplicationContext().addApplicationListener(ui);
            ui.showAndGet();
        }
    }

    class TerminalActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            DataContext dataContext = DataManager.getInstance().getDataContext();
            Project project = dataContext.getData(DataKey.create("project"));
            ToolWindow terminal = ToolWindowManager.getInstance(project).getToolWindow("Terminal");
            terminal.activate(null);
        }
    }

    class RemoveActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ConfigHelper.removeSshServer(sshId);
            ApplicationContext.getApplicationContext().publishEvent(new TableRefreshEvent());
        }
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