package tech.lin2j.idea.plugin.ui;


import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.terminal.TerminalTabState;
import org.jetbrains.plugins.terminal.TerminalView;
import org.jetbrains.plugins.terminal.cloud.CloudTerminalRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.event.TableRefreshEvent;
import tech.lin2j.idea.plugin.enums.AuthType;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.SshStatus;
import tech.lin2j.idea.plugin.ssh.exception.RemoteSdkException;
import tech.lin2j.idea.plugin.ui.editor.ConsoleFileSystem;
import tech.lin2j.idea.plugin.ui.editor.ConsoleVirtualFile;
import tech.lin2j.idea.plugin.ui.ftp.FTPConsoleUi;
import tech.lin2j.idea.plugin.uitl.TerminalRunnerUtil;
import tech.lin2j.idea.plugin.uitl.UiUtil;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
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
        moreBtn.addActionListener(new MoreActionListener());
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
            SshServer tmp = ConfigHelper.getSshServerById(sshId);

            boolean needPassword = AuthType.needPassword(tmp.getAuthType());
            SshServer server = UiUtil.requestPasswordIfNecessary(tmp);
            if (needPassword && StringUtil.isEmpty(server.getPassword())) {
                return;
            }
            SshStatus status = new SshStatus(false, null);
            String title = String.format("Opening terminal %s:%s", server.getIp(), server.getPort());
            ProgressManager.getInstance().run(new Task.Backgroundable(project, title) {
                CloudTerminalRunner runner = null;
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    indicator.setIndeterminate(false);
                    try {
                        runner = TerminalRunnerUtil.createCloudTerminalRunner(project, server);
                        status.setSuccess(true);
                    } catch (RemoteSdkException ex) {
                        status.setMessage("Error connecting server: " + ex.getMessage());
                    } finally {
                        indicator.setFraction(1);
                    }
                }

                @Override
                public void onFinished() {
                    if(!status.isSuccess()) {
                        Messages.showErrorDialog(status.getMessage(), "Error");
                        return ;
                    }
                    TerminalView terminalView = TerminalView.getInstance(project);
                    TerminalTabState tabState = new TerminalTabState();
                    tabState.myTabName = server.getIp() + ":" + server.getPort();
                    terminalView.createNewSession(runner, tabState);
                }
            });
        }
    }

    class MoreActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JPopupMenu menu = new JPopupMenu();
            menu.add(new JMenuItem(new AbstractAction("Properties") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SshServer server = ConfigHelper.getSshServerById(sshId);
                    new HostUi(project, server).showAndGet();
                }
            }));

            menu.add(new JMenuItem(new AbstractAction("SFTP") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SshServer server = ConfigHelper.getSshServerById(sshId);
                    try {
                        ConsoleVirtualFile consoleVirtualFile = new ConsoleVirtualFile(
                                server.getIp(),
                                project,
                                new FTPConsoleUi(project, server)
                        );
                        ConsoleFileSystem.getInstance(project).openEditor(consoleVirtualFile);
                    } catch (IOException ex) {
                        log.error(ex.getMessage(), e);
                    }
                }
            }));

            menu.add(new JMenuItem(new AbstractAction("Remove") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SshServer server = ConfigHelper.getSshServerById(sshId);
                    String specific = "Host: " + server.getIp() + ":" + server.getPort();
                    if (UiUtil.deleteConfirm(specific)) {
                        ConfigHelper.removeSshServer(sshId);
                        ApplicationContext.getApplicationContext().publishEvent(new TableRefreshEvent());
                    }
                }
            }));
            menu.show(moreBtn, 0, moreBtn.getHeight());
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