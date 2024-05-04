package tech.lin2j.idea.plugin.ui;

import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.AdditionalPageAtBottomEditorCustomization;
import com.intellij.ui.EditorCustomization;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.EditorTextFieldProvider;
import com.intellij.ui.SoftWrapsEditorCustomization;
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
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashSet;
import java.util.Set;

/**
 * @author linjinjia
 * @date 2022/4/27 14:50
 */
public class AddCommandUi extends DialogWrapper {
    private static final EditorCustomization BACKGROUND_FROM_COLOR_SCHEME_CUSTOMIZATION = editor -> editor.setBackgroundColor(null);

    private JButton okBtn;
    private JButton cancelBtn;
    private JTextField dirInput;
    private JPanel mainPanel;
    private JTextField titleInput;
    private EditorTextField cmdTextEditor;

    private final Integer sshId;
    private final Integer cmdId;
    private final Project project;

    public AddCommandUi(Project project, Integer sshId,
                        Integer cmdId, String title, String dir, String content) {
        super(true);
        this.sshId = sshId;
        this.cmdId = cmdId;
        this.project = project;
        this.titleInput.setText(title);
        this.dirInput.setText(dir);
        this.cmdTextEditor.setText(content);
        uiInit();
        setTitle("Add Command");
        init();
    }

    public void uiInit() {
        PromptSupport.setPrompt("Please input absolute path", dirInput);
        okBtn.addActionListener(e -> {
            String title = titleInput.getText();
            String dir = dirInput.getText();
            String cmdStr = cmdTextEditor.getText();
            if (StringUtil.isEmpty(dir) || StringUtil.isEmpty(cmdStr) || StringUtil.isEmpty(title)) {
                Messages.showErrorDialog("Title or directory or command must not be null", "Add Command");
                return;
            }
            // update config if command is exist
            if (cmdId != null) {
                Command command = ConfigHelper.getCommandById(cmdId);
                command.setTitle(title);
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

    @NotNull
    private EditorTextField createCommitMessageEditor() {
        Set<EditorCustomization> features = new HashSet<>();

        features.add(SoftWrapsEditorCustomization.ENABLED);
        features.add(AdditionalPageAtBottomEditorCustomization.DISABLED);
        features.add(BACKGROUND_FROM_COLOR_SCHEME_CUSTOMIZATION);
        features.add((editor -> editor.getSettings().setLineNumbersShown(true)));

        EditorTextField editorField =
                EditorTextFieldProvider.getInstance().getEditorField(FileTypes.PLAIN_TEXT.getLanguage(), project, features);

        // Global editor color scheme is set by EditorTextField logic. We also need to use font from it and not from the current LaF.
        editorField.setFontInheritedFromLAF(false);
        return editorField;
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

    private void createUIComponents() {
        cmdTextEditor = createCommitMessageEditor();
        cmdTextEditor.setPreferredSize(new Dimension(400, 300));
    }
}