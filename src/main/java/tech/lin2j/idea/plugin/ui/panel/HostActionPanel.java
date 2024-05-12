package tech.lin2j.idea.plugin.ui.panel;

import com.intellij.openapi.project.Project;
import tech.lin2j.idea.plugin.action.CommandDialogAction;
import tech.lin2j.idea.plugin.action.HostMoreOpsAction;
import tech.lin2j.idea.plugin.action.OpenTerminalAction;
import tech.lin2j.idea.plugin.action.UploadDialogAction;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GridLayout;

/**
 * @author linjinjia
 * @date 2024/5/5 01:22
 */
public class HostActionPanel extends JPanel {

    private JButton uploadBtn;
    private JButton commandBtn;
    private JButton terminalBtn;
    private JButton moreActionBtn;

    private final int sshId;
    private final Project project;

    public HostActionPanel(int sshId, Project project) {
        this.sshId = sshId;
        this.project = project;
        setLayout(new GridLayout(1, 4));

        initButtons(sshId);
        add(uploadBtn);
        add(commandBtn);
        add(terminalBtn);
        add(moreActionBtn);
    }

    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        setBackgroundColor(uploadBtn, bg);
        setBackgroundColor(commandBtn, bg);
        setBackgroundColor(terminalBtn, bg);
        setBackgroundColor(moreActionBtn, bg);
    }

    private void initButtons(int sshId) {
        uploadBtn = new JButton(MessagesBundle.getText("table.action.button.upload"));
        commandBtn = new JButton(MessagesBundle.getText("table.action.button.command"));
        terminalBtn = new JButton(MessagesBundle.getText("table.action.button.terminal"));
        moreActionBtn = new JButton(MessagesBundle.getText("table.action.button.more") + " ▼");


        uploadBtn.addActionListener(new UploadDialogAction(sshId, project));
        commandBtn.addActionListener(new CommandDialogAction(sshId, project));
        terminalBtn.addActionListener(new OpenTerminalAction(sshId, project));
        moreActionBtn.addActionListener(new HostMoreOpsAction(sshId, project, moreActionBtn));
    }

    public String getState() {
        return sshId + "";
    }

    private void setBackgroundColor(JButton btn, Color bg) {
        if (btn != null) {
            btn.setBackground(bg);
        }
    }
}