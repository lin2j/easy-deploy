package tech.lin2j.idea.plugin.ui;


import com.intellij.openapi.project.Project;
import com.intellij.remote.RemoteCredentialsHolder;
//import com.jetbrains.plugins.remotesdk.console.SshTerminalDirectRunner;
import com.jetbrains.plugins.remotesdk.console.SshTerminalDirectRunner;
import org.jetbrains.plugins.terminal.TerminalView;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.SshServer;
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
import java.nio.charset.Charset;
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
    private final Project project;

    private int verticalAlignment = CENTER;

    Map<String, TableActionUi> uiMap = new ConcurrentHashMap<>();

    public TableActionUi(Project project) {
        super();
        setName("Table.cellRenderer");

        this.project = project;

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
            ui = new TableActionUi(project);
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
            UploadUi ui = new UploadUi(project, ConfigHelper.getSshServerById(sshId));
            ApplicationContext.getApplicationContext().addApplicationListener(ui);
            ui.showAndGet();
        }
    }

    class ExecuteActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            SelectCommandUi ui = new SelectCommandUi(project, sshId);
            ApplicationContext.getApplicationContext().addApplicationListener(ui);
            ui.showAndGet();
        }
    }

    class TerminalActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            SshServer server = ConfigHelper.getSshServerById(sshId);
            RemoteCredentialsHolder credentials = new RemoteCredentialsHolder();
            credentials.setHost(server.getIp());
            credentials.setPort(server.getPort());
            credentials.setUserName(server.getUsername());
            credentials.setPassword(server.getPassword());

            SshTerminalDirectRunner runner = new SshTerminalDirectRunner(project, credentials, Charset.defaultCharset());
            TerminalView terminalView = TerminalView.getInstance(project);
            terminalView.createNewSession(runner);
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