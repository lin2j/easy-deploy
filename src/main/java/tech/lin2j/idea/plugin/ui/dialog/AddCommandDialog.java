package tech.lin2j.idea.plugin.ui.dialog;

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
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.domain.model.Command;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.event.CommandAddEvent;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.util.HashSet;
import java.util.Set;

/**
 * @author linjinjia
 * @date 2024/5/4 15:24
 */
public class AddCommandDialog extends DialogWrapper {
    private final JPanel root;

    private JBTextField titleInput;
    private JBTextField pathInput;
    private EditorTextField commandEditor;

    private final Project project;
    private final Command command;

    public AddCommandDialog(@NotNull Project project, @NotNull Command command) {
        super(project);
        this.project = project;
        this.command = command;

        initInput();
        initCommandEditor();
        setContent();

        root = FormBuilder.createFormBuilder()
                .addLabeledComponent(MessagesBundle.getText("dialog.command.title"), titleInput)
                .addLabeledComponent(MessagesBundle.getText("dialog.command.path"), pathInput)
                .addLabeledComponent(MessagesBundle.getText("dialog.command.content"), commandEditor, true)
                .getPanel();

        setTitle(MessagesBundle.getText("dialog.command.frame"));
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return root;
    }

    @Override
    protected void doOKAction() {
        String title = titleInput.getText();
        String dir = pathInput.getText();
        String cmdStr = commandEditor.getText();
        if (StringUtil.isEmpty(dir) || StringUtil.isEmpty(cmdStr) || StringUtil.isEmpty(title)) {
            Messages.showErrorDialog(
                    MessagesBundle.getText("dialog.command.error"),
                    MessagesBundle.getText("dialog.command.frame")
            );
            return;
        }
        // update config if command is exist
        Integer cmdId = command.getId();
        Integer sshId = command.getSshId();
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
        ApplicationContext.getApplicationContext().publishEvent(new CommandAddEvent());
        super.doOKAction();
    }

    private void setContent() {
        titleInput.setText(command.getTitle());
        pathInput.setText(command.getDir());
        commandEditor.setText(command.getContent());
    }

    private void initInput() {
        titleInput = new JBTextField();
        pathInput = new JBTextField();
        pathInput.getEmptyText().setText(MessagesBundle.getText("dialog.command.path.tip"));
    }

    private void initCommandEditor() {
        Set<EditorCustomization> features = new HashSet<>();
        features.add(SoftWrapsEditorCustomization.ENABLED);
        features.add(AdditionalPageAtBottomEditorCustomization.DISABLED);
        features.add(editor -> editor.setBackgroundColor(null));
        features.add((editor -> editor.getSettings().setLineNumbersShown(true)));

        commandEditor =
                EditorTextFieldProvider.getInstance().getEditorField(FileTypes.PLAIN_TEXT.getLanguage(), project, features);

        // Global editor color scheme is set by EditorTextField logic.
        // We also need to use font from it and not from the current LaF.
        commandEditor.setFontInheritedFromLAF(false);
        commandEditor.setPreferredSize(new Dimension(500, 350));
    }
}