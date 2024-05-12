package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.domain.model.Command;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.NoneCommand;
import tech.lin2j.idea.plugin.domain.model.UploadProfile;
import tech.lin2j.idea.plugin.domain.model.event.UploadProfileAddEvent;
import tech.lin2j.idea.plugin.domain.model.event.UploadProfileSelectedEvent;
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
import java.util.Objects;

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
                .addLabeledComponent(MessagesBundle.getText("dialog.profile.add.name"), nameInput)
                .addLabeledComponent(MessagesBundle.getText("dialog.profile.add.file"), fileBrowser)
                .addLabeledComponent(MessagesBundle.getText("dialog.profile.add.exclude"), excludeInput)
                .addLabeledComponent(MessagesBundle.getText("dialog.profile.add.location"), locationInput)
                .addLabeledComponent(MessagesBundle.getText("dialog.profile.add.command"), commandBoxContainer)
                .getPanel();
        root.setPreferredSize(new Dimension(UiUtil.screenWidth() / 2, 0));

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
            profile.setName(name);
            profile.setSshId(sshId);
            profile.setFile(file);
            profile.setExclude(exclude);
            profile.setLocation(location);
            profile.setCommandId(command == NoneCommand.INSTANCE ? null : command.getId());
            profile.setSelected(true);
            ApplicationContext.getApplicationContext().publishEvent(new UploadProfileSelectedEvent(profile));
        } else {
            UploadProfile newProfile = new UploadProfile();
            newProfile.setId(ConfigHelper.maxUploadProfileId() + 1);
            newProfile.setName(name);
            newProfile.setSshId(sshId);
            newProfile.setFile(file);
            newProfile.setExclude(exclude);
            newProfile.setLocation(location);
            newProfile.setCommandId(command == NoneCommand.INSTANCE ? null : command.getId());
            newProfile.setSelected(true);

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
    }

    private void initCommandBox() {
        Integer sshId = profile.getSshId();
        commandBox = new ComboBox<>();
        commandBox.setRenderer(new CommandColoredListCellRenderer());
        commandBox.addItem(NoneCommand.INSTANCE);
        ConfigHelper.getCommandsBySshId(sshId).forEach(cmd -> commandBox.addItem(cmd));

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        commandBoxContainer = new JPanel(layout);
        commandBoxContainer.add(commandBox, gbc);
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
        nameInput.setText(profile.getName());
        fileBrowser.setText(profile.getFile());
        if (FileUtil.isDirectory(profile.getFile())) {
            excludeInput.setEnabled(true);
            excludeInput.setText(profile.getExclude());
        }
        locationInput.setText(profile.getLocation());
        for (int i = 0; i < commandBox.getItemCount(); i++) {
            Command command = commandBox.getItemAt(i);
            if (Objects.equals(command.getId(), profile.getCommandId())) {
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

    private FileChooserDescriptor allButNoMultipleChoose() {
        return new FileChooserDescriptor(true, true, true, true, true, false);
    }
}