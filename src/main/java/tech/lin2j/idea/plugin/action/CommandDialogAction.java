package tech.lin2j.idea.plugin.action;

import com.intellij.openapi.project.Project;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.ui.dialog.SelectCommandDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author linjinjia
 * @date 2024/5/5 12:26
 */
public class CommandDialogAction implements ActionListener {

    private final int sshId;
    private final Project project;

    public CommandDialogAction(int sshId, Project project) {
        this.sshId = sshId;
        this.project = project;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SelectCommandDialog ui = new SelectCommandDialog(project, sshId);
        ApplicationContext.getApplicationContext().addApplicationListener(ui);
        ui.showAndGet();
    }
}