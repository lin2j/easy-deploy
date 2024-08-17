package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
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
import com.intellij.ui.ToggleActionButton;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import icons.MyIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.action.CopyCommandAction;
import tech.lin2j.idea.plugin.action.PasteCommandAction;
import tech.lin2j.idea.plugin.model.Command;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.event.CommandAddEvent;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author linjinjia
 * @date 2024/5/4 15:24
 */
public class AddCommandDialog extends DialogWrapper {
    private final JPanel root;

    private JBTextField titleInput;
    private JBTextField pathInput;
    private EditorTextField commandEditor;
    private boolean shareCommand;
    private ToggleActionButton shareToggleButton;
    private boolean shareBtnInitialized;

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
                .addLabeledComponent(MessagesBundle.getText("dialog.command.title"), titleRow())
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
        title = StringUtil.trim(title);
        dir = StringUtil.trim(dir);
        cmdStr = StringUtil.trim(cmdStr);

        // update config if command is exist
        Integer cmdId = command.getId();
        Integer sshId = command.getSshId();
        if (cmdId != null) {
            Command command = ConfigHelper.getCommandById(cmdId);
            command.setTitle(title);
            command.setDir(dir);
            command.setContent(cmdStr);
            command.setSharable(shareCommand);
        } else {
            cmdId = ConfigHelper.maxCommandId() + 1;
            Command cmd = new Command(cmdId, sshId, title, dir, cmdStr, shareCommand);
            cmd.setUid(UUID.randomUUID().toString());
            ConfigHelper.addCommand(cmd);
        }
        ApplicationContext.getApplicationContext().publishEvent(new CommandAddEvent());
        super.doOKAction();
    }

    private JComponent titleRow() {
        if (!shareBtnInitialized) {
            Icon icon = shareCommand ? MyIcons.Actions.Shared : MyIcons.Actions.Share;
            shareToggleButton = new ToggleActionButton("Sharable Command", icon) {
                @Override
                public boolean isSelected(AnActionEvent e) {
                    Icon icon = shareCommand ? MyIcons.Actions.Shared : MyIcons.Actions.Share;
                    e.getPresentation().setIcon(icon);
                    return shareCommand;
                }

                @Override
                public void setSelected(AnActionEvent e, boolean b) {
                    shareCommand = b;
                }
            };
            shareBtnInitialized = true;
        }

        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new CopyCommandAction(command));
        group.add(new PasteCommandAction(this::setContent));
        group.add(shareToggleButton);

        ActionToolbar toolbar = ActionManager.getInstance()
                .createActionToolbar("AddDialoag@Toolbar", group, true);
        toolbar.setTargetComponent(null);

        final JPanel titlePanel = new JPanel(new GridBagLayout());
        titlePanel.add(titleInput, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));
        titlePanel.add(toolbar.getComponent(), new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));

        return titlePanel;
    }

    private void setContent() {
        setContent(command);
    }

    private void setContent(Command cmd) {
        titleInput.setText(cmd.getTitle());
        pathInput.setText(cmd.getDir());
        commandEditor.setText(cmd.getContent());
        shareCommand = cmd.getSharable();
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