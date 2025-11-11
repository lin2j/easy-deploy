package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.ui.component.*;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.*;
import java.awt.*;

public class CommandManageDialog extends DialogWrapper {
    private final Project project;
    private final JPanel root = new JPanel(new BorderLayout());

    private final QuickCommandPanel quickCommandPanel;
//    private final CommandManagePanel commandManagePanel;
//    private final CommandSettingsPanel commandSettingsPanel;

    protected CommandManageDialog(@Nullable Project project) {
        super(project);
        this.project = project;
//        JButton testButton = new JButton(MessagesBundle.getText("dialog.panel.host.test-connect"));
//        testButton.addActionListener(this::testConnect);

        quickCommandPanel = new QuickCommandPanel(project);
//        commandManagePanel = new HostProxyPanel(project, server);
//        commandSettingsPanel = new HostOtherPanel(server);

        setTitle(MessagesBundle.getText("dialog.host.title"));
        setSize(500, 0);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return null;
    }
}
