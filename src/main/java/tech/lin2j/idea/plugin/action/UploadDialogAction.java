package tech.lin2j.idea.plugin.action;

import com.intellij.openapi.project.Project;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.ui.dialog.UploadProfileDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author linjinjia
 * @date 2024/5/5 12:26
 */
public class UploadDialogAction implements ActionListener {

    private final int sshId;
    private final Project project;

    public UploadDialogAction(int sshId, Project project) {
        this.sshId = sshId;
        this.project = project;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        UploadProfileDialog dialog = new UploadProfileDialog(project, ConfigHelper.getSshServerById(sshId));
        ApplicationContext.getApplicationContext().addApplicationListener(dialog);
        dialog.showAndGet();
    }
}