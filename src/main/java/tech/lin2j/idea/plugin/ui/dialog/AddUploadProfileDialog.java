package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.action.AddCommandAction;
import tech.lin2j.idea.plugin.action.CopyUploadProfileAction;
import tech.lin2j.idea.plugin.action.PasteUploadProfileAction;
import tech.lin2j.idea.plugin.model.Command;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.NoneCommand;
import tech.lin2j.idea.plugin.model.SeparatorCommand;
import tech.lin2j.idea.plugin.model.UploadProfile;
import tech.lin2j.idea.plugin.model.event.UploadProfileAddEvent;
import tech.lin2j.idea.plugin.model.event.UploadProfileSelectedEvent;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.ui.render.CommandColoredListCellRenderer;
import tech.lin2j.idea.plugin.uitl.FileUtil;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;
import tech.lin2j.idea.plugin.uitl.UiUtil;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author linjinjia
 * @date 2024/5/4 16:03
 */
public class AddUploadProfileDialog extends DialogWrapper {

    private final JPanel root;

    private JBTextField nameInput;
    private JBTextField excludeInput;
    private JBTextField locationInput;
    private TextFieldWithBrowseButton fileBrowser;
    private JPanel commandBoxContainer;
    private ComboBox<Command> commandBox;
    private JBLabel ignored;

    private final Project project;
    private final UploadProfile profile;

    public AddUploadProfileDialog(@Nullable Project project, @NotNull UploadProfile profile) {
        super(project);
        this.project = project;
        this.profile = profile;

        initInput();
        initCommandBox();
        initFileBrowser();
        setContent();

        root = FormBuilder.createFormBuilder()
                .addLabeledComponent(MessagesBundle.getText("dialog.profile.add.name"), nameRow())
                .addLabeledComponent(MessagesBundle.getText("dialog.profile.add.file"), fileBrowser)
                .addLabeledComponent(MessagesBundle.getText("dialog.profile.add.exclude"), excludeInput)
                .addLabeledComponent(MessagesBundle.getText("dialog.profile.add.location"), locationInput)
                .addLabeledComponent(MessagesBundle.getText("dialog.profile.add.command"), commandBoxContainer)
                .addComponent(ignored)
                .getPanel();

        setTitle(MessagesBundle.getText("dialog.profile.add.frame"));
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return root;
    }

    @Override
    protected void doOKAction() {
        String name = nameInput.getText();
        String file = fileBrowser.getText();
        String location = locationInput.getText();
        Command command = (Command) commandBox.getSelectedItem();

        String exclude = excludeInput.getText();
        if (!FileUtil.isDirectory(file)) {
            exclude = "";
        }

        // update config if profile is exist
        Integer sshId = profile.getSshId();
        if (profile.getId() != null) {
            profile.setSshId(sshId);
            profile.setName(trim(name));
            profile.setFile(trim(file));
            profile.setExclude(trim(exclude));
            profile.setLocation(trim(location));
            profile.setCommandId(getCommandId(command));
            profile.setSelected(true);
            ApplicationContext.getApplicationContext().publishEvent(new UploadProfileSelectedEvent(profile));
        } else {
            UploadProfile newProfile = new UploadProfile();
            newProfile.setId(ConfigHelper.maxUploadProfileId() + 1);
            newProfile.setName(trim(name));
            newProfile.setSshId(sshId);
            newProfile.setFile(trim(file));
            newProfile.setExclude(trim(exclude));
            newProfile.setLocation(trim(location));
            newProfile.setCommandId(getCommandId(command));
            newProfile.setSelected(true);
            newProfile.setUid(UUID.randomUUID().toString());

            ConfigHelper.addUploadProfile(newProfile);
            ApplicationContext.getApplicationContext().publishEvent(new UploadProfileSelectedEvent(newProfile));
        }

        ApplicationContext.getApplicationContext().publishEvent(new UploadProfileAddEvent());

        super.doOKAction();
    }

    private void initInput() {
        nameInput = new JBTextField();
        locationInput = new JBTextField();
        excludeInput = new JBTextField();
        excludeInput.getEmptyText().setText("*.log;*.iml");

        ignored = new JBLabel();
        ignored.setPreferredSize(new Dimension(UiUtil.screenWidth() / 2 - 40, 0));
    }

    private void initCommandBox() {
        Integer sshId = profile.getSshId();

        List<Command> data = new ArrayList<>();
        data.add(NoneCommand.INSTANCE);
        data.addAll(ConfigHelper.getCommandsBySshId(sshId));
        data.add(SeparatorCommand.INSTANCE);
        data.addAll(ConfigHelper.getSharableCommands(sshId));

        commandBox = new ComboBox<>(new CollectionComboBoxModel<>(data));
        commandBox.setRenderer(new CommandColoredListCellRenderer(sshId));

        // Add command button
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new AddCommandAction(project, sshId, this::addNewCommand));
        ActionToolbar toolbar = ActionManager.getInstance()
                .createActionToolbar("AddUploadProfile@AddCommand", group, true);
        toolbar.setTargetComponent(null);

        commandBoxContainer = new JPanel(new GridBagLayout());
        commandBoxContainer.add(commandBox, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));
        commandBoxContainer.add(toolbar.getComponent(), new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));
    }

    private JPanel nameRow() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new CopyUploadProfileAction(profile));
        group.add(new PasteUploadProfileAction(this::setContent));

        ActionToolbar toolbar = ActionManager.getInstance()
                .createActionToolbar("AddUploadProfile@Toolbar", group, true);
        toolbar.setTargetComponent(null);

        final JPanel namePanel = new JPanel(new GridBagLayout());
        namePanel.add(nameInput, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));
        namePanel.add(toolbar.getComponent(), new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));

        return namePanel;
    }

    private void initFileBrowser() {
        fileBrowser = new TextFieldWithBrowseButton();
        fileBrowser.addActionListener(e -> {
            FileChooserDescriptor descriptor = allButNoMultipleChoose();
            VirtualFile virtualFile = FileChooser.chooseFile(descriptor, fileBrowser, project,  getCurrentWorkingDir());
            if (virtualFile != null) {
                fileBrowser.setText(virtualFile.getPath());
                excludeInput.setEnabled(virtualFile.isDirectory());
            }
        });
    }

    private void setContent() {
        setContent(profile);
    }

    private void setContent(UploadProfile up) {
        nameInput.setText(up.getName());
        fileBrowser.setText(up.getFile());
        if (FileUtil.isDirectory(up.getFile())) {
            excludeInput.setEnabled(true);
            excludeInput.setText(up.getExclude());
        }
        locationInput.setText(up.getLocation());
        for (int i = 0; i < commandBox.getItemCount(); i++) {
            Command command = commandBox.getItemAt(i);
            if (command instanceof SeparatorCommand) {
                continue;
            }
            if (Objects.equals(command.getId(), up.getCommandId())) {
                commandBox.setSelectedIndex(i);
            }
        }
    }

    @Nullable
    private VirtualFile getCurrentWorkingDir() {
        String dir = project != null ? project.getBasePath() : null;
        VirtualFile result = null;
        if (dir != null) {
            result = LocalFileSystem.getInstance().findFileByPath(dir);
        }
        return result;
    }

    private String trim(String s) {
        if (StringUtil.isNotEmpty(s)) {
            return s.trim();
        }
        return s;
    }

    private Integer getCommandId(Command cmd) {
       if (cmd instanceof NoneCommand || cmd instanceof SeparatorCommand) {
           return null;
       }
       return cmd == null ? null : cmd.getId();
    }

    private void addNewCommand(Command cmd) {
        CollectionComboBoxModel<Command> model = (CollectionComboBoxModel<Command>) commandBox.getModel();
        List<Command> items = model.getItems();
        int i = 0;
        for (Command item : items) {
            if (item instanceof SeparatorCommand) {
                model.add(i, cmd);
                break;
            }
            i++;
        }
    }

    private FileChooserDescriptor allButNoMultipleChoose() {
        return new FileChooserDescriptor(true, true, true, true, true, false);
    }

}