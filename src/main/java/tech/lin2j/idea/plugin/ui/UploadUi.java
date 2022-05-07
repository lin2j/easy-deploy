package tech.lin2j.idea.plugin.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.CommandUtil;
import tech.lin2j.idea.plugin.domain.model.Command;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.NoneCommand;
import tech.lin2j.idea.plugin.domain.model.SshServer;
import tech.lin2j.idea.plugin.domain.model.SshStatus;
import tech.lin2j.idea.plugin.domain.model.SshUpload;
import tech.lin2j.idea.plugin.service.SshService;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;

/**
 * @author linjinjia
 * @date 2022/4/25 22:25
 */
public class UploadUi extends DialogWrapper {
    private JButton browserBtn;
    private JButton uploadBtn;
    private JButton cancelBtn;
    private JPanel mainPanel;
    private JLabel hostLabel;
    private JComboBox<Command> cmdList;
    private JComboBox<String> fileInputBox;
    private JComboBox<String> locationBox;

    private SshServer sshServer;
    private SshService sshService = SshService.getInstance();

    public UploadUi(SshServer sshServer) {
        super(true);
        this.sshServer = sshServer;
        uiInit();
        setTitle("Upload");
        init();
    }

    private void uiInit() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        mainPanel.setMinimumSize(new Dimension(tk.getScreenSize().width / 2, 0));

        hostLabel.setText(sshServer.getIp());

        locationBox.setEditable(true);
        List<SshUpload> sshUploadList = ConfigHelper.getSshUploadsBySshId(sshServer.getId());
        if (sshUploadList.size() > 0) {
            for (int i = 0; i < sshUploadList.size(); i++) {
                SshUpload sshUpload = sshUploadList.get(i);
                // selected or the last one in the list
                boolean select = sshUpload.getSelected() || i == sshUploadList.size() - 1;
                if (select) {
                    fileInputBox.addItem(sshUpload.getLocalFile());
                    locationBox.addItem(sshUpload.getRemoteFile());
                }
            }
        }

        browserBtn.addActionListener(e -> {
            Project project = ProjectManager.getInstance().getDefaultProject();
            FileChooserDescriptor chooserDescriptor = new FileChooserDescriptor(true, true, true, true, true, true);
            VirtualFile virtualFile = FileChooser.chooseFile(chooserDescriptor, project, null);
            if (virtualFile != null) {
                jComboBoxSet(fileInputBox, virtualFile.getPath());
            }
        });

        cancelBtn.addActionListener(e -> close(CANCEL_EXIT_CODE));

        uploadBtn.addActionListener(e -> {
            String filePath = (String) fileInputBox.getSelectedItem();
            String location = (String) locationBox.getSelectedItem();

            boolean add = ConfigHelper.addSshUpload(new SshUpload(sshServer.getId(), filePath, location));
            // location may not exist in the box, because the box is editable
            // add is true if the location is not exist
            if (add) {
                jComboBoxSet(locationBox, location);
            }

            SshStatus status = sshService.scpPut(sshServer, filePath, location);
            if (!status.isSuccess()) {
                Messages.showErrorDialog(status.getMessage(), "Upload");
                return;
            }
            Command cmd = (Command) cmdList.getSelectedItem();
            if (NoneCommand.INSTANCE.equals(cmd)) {
                Messages.showInfoMessage("Upload success", "Upload");
                close(OK_EXIT_CODE);
                return;
            }
            CommandUtil.executeAndShowMessages(cmd, sshServer, this);
        });

        cmdList.addItem(NoneCommand.INSTANCE);
        ConfigHelper.getCommandsBySshId(sshServer.getId()).forEach(cmd -> cmdList.addItem(cmd));
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel dialog = new JPanel(new BorderLayout());
        dialog.add(mainPanel, BorderLayout.CENTER);
        return dialog;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{};
    }

    private void jComboBoxSet(JComboBox<String> box, String value) {
        boolean isRepeat = false;
        int repeatIndex = 0;
        for (int i = 0; i < box.getItemCount(); i++) {
            isRepeat = box.getItemAt(i).equals(value);
            if (isRepeat) {
                repeatIndex = i;
                break;
            }
        }
        // select the item if it is repeating, or add the item into the box
        if (isRepeat) {
            box.setSelectedIndex(repeatIndex);
        } else {
            box.addItem(value);
            box.setSelectedIndex(box.getItemCount() - 1);
        }
    }
}