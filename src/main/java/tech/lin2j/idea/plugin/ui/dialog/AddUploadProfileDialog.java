package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
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
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.action.AddCommandAction;
import tech.lin2j.idea.plugin.action.CopyUploadProfileAction;
import tech.lin2j.idea.plugin.action.PasteUploadProfileAction;
import tech.lin2j.idea.plugin.event.ApplicationContext;
import tech.lin2j.idea.plugin.model.*;
import tech.lin2j.idea.plugin.model.event.UploadProfileAddEvent;
import tech.lin2j.idea.plugin.model.event.UploadProfileSelectedEvent;
import tech.lin2j.idea.plugin.ui.render.CommandColoredListCellRenderer;
import tech.lin2j.idea.plugin.uitl.FileUtil;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;
import tech.lin2j.idea.plugin.uitl.UiUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static tech.lin2j.idea.plugin.enums.Constant.LOCAL_FILE_INFO_SEPARATOR;
import static tech.lin2j.idea.plugin.enums.Constant.STR_FALSE;
import static tech.lin2j.idea.plugin.enums.Constant.STR_TRUE;

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
    private JPanel fileContainer;
    private boolean useRegex;
    private JPanel preCommandBoxContainer;
    private JPanel postCommandBoxContainer;
    private ComboBox<Command> preCommandBox;
    private ComboBox<Command> postCommandBox;
    private SimpleColoredComponent preCommandPreview;
    private SimpleColoredComponent postCommandPreview;
    private JBCheckBox useUploadPathCheckBox;
    private JBLabel ignored;

    private final Project project;
    private final UploadProfile profile;

    public AddUploadProfileDialog(@Nullable Project project, @NotNull UploadProfile profile) {
        super(project);
        this.project = project;
        this.profile = profile;

        initInput();
        initCommandBoxes();
        initFileBrowser();
        setContent();

        root = FormBuilder.createFormBuilder()
                .addLabeledComponent(MessagesBundle.getText("dialog.profile.add.name"), nameRow())
                .addLabeledComponent(MessagesBundle.getText("dialog.profile.add.file"), fileContainer)
                .addLabeledComponent(MessagesBundle.getText("dialog.profile.add.exclude"), excludeInput)
                .addLabeledComponent(MessagesBundle.getText("dialog.profile.add.location"), locationInput)
                .addLabeledComponent(MessagesBundle.getText("dialog.upload.pre-command"), preCommandBoxContainer)
                .addLabeledComponent(MessagesBundle.getText("dialog.upload.post-command"), postCommandBoxContainer)
                .addComponent(useUploadPathCheckBox)
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
        String file = fileBrowser.getText() + LOCAL_FILE_INFO_SEPARATOR + (useRegex ? STR_TRUE : STR_FALSE);
        String location = locationInput.getText();
        Command preCommand = (Command) preCommandBox.getSelectedItem();
        Command postCommand = (Command) postCommandBox.getSelectedItem();

        String exclude = excludeInput.getText();

        // update config if profile is exist
        Integer sshId = profile.getSshId();
        if (profile.getId() != null) {
            profile.setSshId(sshId);
            profile.setName(trim(name));
            profile.setFile(trim(file));
            profile.setExclude(trim(exclude));
            profile.setLocation(trim(location));
            profile.setPreCommandId(getCommandId(preCommand));
            profile.setPostCommandId(getCommandId(postCommand));
            profile.setUseUploadPath(useUploadPathCheckBox.isSelected());
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
            newProfile.setPreCommandId(getCommandId(preCommand));
            newProfile.setPostCommandId(getCommandId(postCommand));
            newProfile.setUseUploadPath(useUploadPathCheckBox.isSelected());
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
        locationInput.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateCommandPreview();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateCommandPreview();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateCommandPreview();
            }
        });
        excludeInput = new JBTextField();
        excludeInput.getEmptyText().setText("*.log;*.iml");

        useUploadPathCheckBox = new JBCheckBox(MessagesBundle.getText("dialog.profile.add.use-upload-path"));
        useUploadPathCheckBox.setToolTipText(MessagesBundle.getText("dialog.profile.add.use-upload-path.tooltip"));

        ignored = new JBLabel();
        ignored.setPreferredSize(new Dimension(UiUtil.screenWidth() / 2 - 40, 0));
    }

    private void initCommandBoxes() {
        Integer sshId = profile.getSshId();

        List<Command> data = new ArrayList<>();
        data.add(NoneCommand.INSTANCE);
        data.addAll(ConfigHelper.getCommandsBySshId(sshId));
        data.add(SeparatorCommand.INSTANCE);
        data.addAll(ConfigHelper.getSharableCommands(sshId));

        // Pre-upload command box
        preCommandBox = new ComboBox<>(new CollectionComboBoxModel<>(new ArrayList<>(data)));
        preCommandBox.setRenderer(new CommandColoredListCellRenderer(sshId));
        preCommandBox.addItemListener(e -> updateCommandPreview());

        // Post-upload command box
        postCommandBox = new ComboBox<>(new CollectionComboBoxModel<>(new ArrayList<>(data)));
        postCommandBox.setRenderer(new CommandColoredListCellRenderer(sshId));
        postCommandBox.addItemListener(e -> updateCommandPreview());

        // Initialize command preview components
        preCommandPreview = new SimpleColoredComponent();
        postCommandPreview = new SimpleColoredComponent();

        // Add command button for pre-upload
        DefaultActionGroup preGroup = new DefaultActionGroup();
        preGroup.add(new AddCommandAction(project, sshId, cmd -> addNewCommand(cmd, true)));
        ActionToolbar preToolbar = ActionManager.getInstance()
                .createActionToolbar("AddUploadProfile@AddPreCommand", preGroup, true);
        preToolbar.setTargetComponent(null);

        preCommandBoxContainer = new JPanel(new GridBagLayout());
        preCommandBoxContainer.add(preCommandBox, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));
        preCommandBoxContainer.add(preToolbar.getComponent(), new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));
        preCommandBoxContainer.add(preCommandPreview, new GridBagConstraints(0, 1, 2, 1, 1, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.insets(2, 0), 0, 0));

        // Add command button for post-upload
        DefaultActionGroup postGroup = new DefaultActionGroup();
        postGroup.add(new AddCommandAction(project, sshId, cmd -> addNewCommand(cmd, false)));
        ActionToolbar postToolbar = ActionManager.getInstance()
                .createActionToolbar("AddUploadProfile@AddPostCommand", postGroup, true);
        postToolbar.setTargetComponent(null);

        postCommandBoxContainer = new JPanel(new GridBagLayout());
        postCommandBoxContainer.add(postCommandBox, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));
        postCommandBoxContainer.add(postToolbar.getComponent(), new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));
        postCommandBoxContainer.add(postCommandPreview, new GridBagConstraints(0, 1, 2, 1, 1, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.insets(2, 0), 0, 0));
        
        // Add listener for useUploadPathCheckBox to update command preview
        useUploadPathCheckBox.addActionListener(e -> updateCommandPreview());
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
            }
        });

        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new RegexToggleAction());
        ActionToolbar uploadToolbar = ActionManager.getInstance()
                .createActionToolbar("AddUploadProfileDialog@UseRegex", group, true);
        uploadToolbar.setTargetComponent(null);
        fileContainer = new JPanel(new GridBagLayout());
        fileContainer.add(fileBrowser, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));
        fileContainer.add(uploadToolbar.getComponent(), new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));
    }

    private void setContent() {
        setContent(profile);
    }

    private void setContent(UploadProfile up) {
        nameInput.setText(up.getName());
        if (up.getFile() != null) {
            String[] split = up.getFile().split(LOCAL_FILE_INFO_SEPARATOR);
            fileBrowser.setText(split[0]);
            useRegex = split.length == 2 && Objects.equals(split[1], STR_TRUE);
        }
        if (FileUtil.isDirectory(up.getFile())) {
            excludeInput.setEnabled(true);
            excludeInput.setText(up.getExclude());
        }
        locationInput.setText(up.getLocation());
        useUploadPathCheckBox.setSelected(up.getUseUploadPath() != null && up.getUseUploadPath());
        
        // Handle backward compatibility: if commandId exists but pre/postCommandId don't, migrate to postCommandId
        Integer preCommandId = up.getPreCommandId();
        Integer postCommandId = up.getPostCommandId();
        if (preCommandId == null && postCommandId == null && up.getCommandId() != null) {
            postCommandId = up.getCommandId();
        }
        
        // Set pre-upload command
        selectCommandInBox(preCommandBox, preCommandId);
        
        // Set post-upload command
        selectCommandInBox(postCommandBox, postCommandId);
        
        // Update command preview after setting content
        updateCommandPreview();
    }
    
    private void updateCommandPreview() {
        String overrideDir = useUploadPathCheckBox.isSelected() ? locationInput.getText() : null;
        
        // Update pre-command preview
        preCommandPreview.clear();
        Command preCommand = (Command) preCommandBox.getSelectedItem();
        if (preCommand != null && !(preCommand instanceof NoneCommand) && !(preCommand instanceof SeparatorCommand)) {
            if (StringUtil.isNotEmpty(preCommand.getTitle())) {
                preCommandPreview.append(preCommand.getTitle() + " ");
            }
            preCommandPreview.append(preCommand.toDisplayString(overrideDir), SimpleTextAttributes.GRAY_ATTRIBUTES);
        }
        
        // Update post-command preview
        postCommandPreview.clear();
        Command postCommand = (Command) postCommandBox.getSelectedItem();
        if (postCommand != null && !(postCommand instanceof NoneCommand) && !(postCommand instanceof SeparatorCommand)) {
            if (StringUtil.isNotEmpty(postCommand.getTitle())) {
                postCommandPreview.append(postCommand.getTitle() + " ");
            }
            postCommandPreview.append(postCommand.toDisplayString(overrideDir), SimpleTextAttributes.GRAY_ATTRIBUTES);
        }
    }
    
    private void selectCommandInBox(ComboBox<Command> box, Integer commandId) {
        for (int i = 0; i < box.getItemCount(); i++) {
            Command command = box.getItemAt(i);
            if (command instanceof SeparatorCommand) {
                continue;
            }
            if (Objects.equals(command.getId(), commandId)) {
                box.setSelectedIndex(i);
                break;
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

    private void addNewCommand(Command cmd, boolean isPreCommand) {
        ComboBox<Command> targetBox = isPreCommand ? preCommandBox : postCommandBox;
        CollectionComboBoxModel<Command> model = (CollectionComboBoxModel<Command>) targetBox.getModel();
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

    private class RegexToggleAction extends ToggleAction {
        public RegexToggleAction() {
            super("Regex", "Use regex",AllIcons.Actions.Regex);
        }

        @Override
        public boolean isSelected(@NotNull AnActionEvent e) {
            return useRegex;
        }

        @Override
        public void setSelected(@NotNull AnActionEvent e, boolean state) {
            useRegex = state;
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.BGT;
        }
    }
}