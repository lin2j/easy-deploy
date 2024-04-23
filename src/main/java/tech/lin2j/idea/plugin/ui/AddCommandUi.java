package tech.lin2j.idea.plugin.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import org.jdesktop.swingx.prompt.PromptSupport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.domain.model.Command;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.event.CommandAddEvent;
import tech.lin2j.idea.plugin.event.ApplicationContext;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * @author linjinjia
 * @date 2022/4/27 14:50
 */
public class AddCommandUi extends DialogWrapper {
    private JButton okBtn;
    private JButton cancelBtn;
    private JTextField dirInput;
    private JTextArea cmdContent;
    private JPanel mainPanel;
    private JScrollPane cmdScroll;
    private JTextField titleInput;

    private final Integer sshId;
    private final Integer cmdId;

    public AddCommandUi(Integer sshId, Integer cmdId, String title, String dir, String content) {
        super(true);
        this.sshId = sshId;
        this.cmdId = cmdId;
        this.titleInput.setText(title);
        this.dirInput.setText(dir);
        this.cmdContent.setText(content);
        uiInit();
        setTitle("Add Command");
        init();
    }

    public void uiInit() {
        cmdScroll.setMinimumSize(new Dimension(400, 300));
        PromptSupport.setPrompt("Please input absolute path", dirInput);
        okBtn.addActionListener(e -> {
            String title = titleInput.getText();
            String dir = dirInput.getText();
            String cmdStr = cmdContent.getText();
            if (StringUtil.isEmpty(dir) || StringUtil.isEmpty(cmdStr) || StringUtil.isEmpty(title)) {
                Messages.showErrorDialog("Title or directory or command must not be null", "Add Command");
                return;
            }
            // update config if command is exist
            if (cmdId != null) {
                Command command = ConfigHelper.getCommandById(cmdId);
                command.setDir(dir);
                command.setContent(cmdStr);
            } else {
                int id = ConfigHelper.maxCommandId() + 1;
                Command cmd = new Command(id, sshId, title, dir, cmdStr);
                ConfigHelper.addCommand(cmd);
            }
            close(OK_EXIT_CODE);
            ApplicationContext.getApplicationContext().publishEvent(new CommandAddEvent());
        });

        cancelBtn.addActionListener(e -> close(CANCEL_EXIT_CODE));
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel dialog = new JPanel(new BorderLayout());
        mainPanel.setVisible(true);
        dialog.add(mainPanel, BorderLayout.CENTER);
        return dialog;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{};
    }
}