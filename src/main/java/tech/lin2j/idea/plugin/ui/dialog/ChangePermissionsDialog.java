package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.xfer.FilePermission;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.file.RemoteTableFile;
import tech.lin2j.idea.plugin.uitl.PosixUtil;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import java.awt.Color;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author linjinjia
 * @date 2024/5/31 23:15
 */
public class ChangePermissionsDialog extends DialogWrapper {
    private final RemoteTableFile file;
    private final SFTPClient sftpClient;

    private final JBLabel permissionLabel;

    private final Set<FilePermission> permissionSet = new HashSet<>();

    public ChangePermissionsDialog(RemoteTableFile file, SFTPClient sftpClient) {
        super(true);
        this.file = file;
        this.sftpClient = sftpClient;

       permissionLabel = new JBLabel("---");
        if (file != null) {
            permissionSet.addAll(file.getFilePermissions());
            permissionLabel.setText(showPermission());
            removeFullPermission();
        }

        setTitle("Change Permission");
        setSize(400, 0);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return FormBuilder.createFormBuilder()
                .addLabeledComponent("Permission:", permissionLabel)
                .addComponent(permissionPanel()).getPanel();
    }

    @Override
    protected void doOKAction() {
        String pattern = "Are you sure you want to change the permissions of [%s] to [%s]?";
        String msg = String.format(pattern, file.getName(), showPermission());
        int no = 1;
        int result = Messages.showYesNoDialog(getContentPanel(),  msg, "Change Permission", null);
        if (Objects.equals(result, no)) {
            return ;
        }
        try {
            sftpClient.chmod(file.getFilePath(), FilePermission.toMask(permissionSet));
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                Messages.showErrorDialog(e.getMessage(), "Change Permission");
            });
        }

        super.doOKAction();
    }

    private JPanel permissionPanel() {
        JPanel permissionPanel = new JPanel(new GridLayout(1, 3));
        JPanel owner = new JPanel(new GridLayout(3, 1));
        JPanel group = new JPanel(new GridLayout(3, 1));
        JPanel others = new JPanel(new GridLayout(3, 1));

        JBCheckBox ownerRead = new PermissionCheckBox("Read", FilePermission.USR_R);
        JBCheckBox ownerWrite = new PermissionCheckBox("Write", FilePermission.USR_W);
        JBCheckBox ownerExec = new PermissionCheckBox("Execute", FilePermission.USR_X);
        JBCheckBox groupRead = new PermissionCheckBox("Read", FilePermission.GRP_R);
        JBCheckBox groupWrite = new PermissionCheckBox("Write", FilePermission.GRP_W);
        JBCheckBox groupExec = new PermissionCheckBox("Execute", FilePermission.GRP_X);
        JBCheckBox otherRead = new PermissionCheckBox("Read", FilePermission.OTH_R);
        JBCheckBox otherWrite = new PermissionCheckBox("Write", FilePermission.OTH_W);
        JBCheckBox otherExec = new PermissionCheckBox("Execute", FilePermission.OTH_X);

        owner.add(ownerRead);
        owner.add(ownerWrite);
        owner.add(ownerExec);
        group.add(groupRead);
        group.add(groupWrite);
        group.add(groupExec);
        others.add(otherRead);
        others.add(otherWrite);
        others.add(otherExec);

        owner.setBorder(new MyTitledBorder("Owner"));
        group.setBorder(new MyTitledBorder("Group"));
        others.setBorder(new MyTitledBorder("Other"));

        permissionPanel.add(owner);
        permissionPanel.add(group);
        permissionPanel.add(others);

        return permissionPanel;
    }

    private static class MyTitledBorder extends TitledBorder {
        private static final Color LINE_COLOR =
                JBColor.namedColor("Group.separatorColor", new JBColor(Gray.xCD, Gray.x51));

        public MyTitledBorder(String title) {
            super(title);
            setBorder(BorderFactory.createLineBorder(LINE_COLOR));
        }
    }

    private class PermissionCheckBox extends JBCheckBox {

        public PermissionCheckBox(String title, FilePermission permission) {
            super(title);

            setSelected(permissionSet.contains(permission));

            addActionListener(e -> {
                if (isSelected()) {
                    permissionSet.add(permission);
                } else {
                    permissionSet.remove(permission);
                }
                permissionLabel.setText(showPermission());
            });
        }
    }

    private String showPermission() {
        int val = FilePermission.toMask(permissionSet);
        return Integer.toOctalString(val) + "   " + PosixUtil.toString(permissionSet);
    }

    private void removeFullPermission() {
        permissionSet.remove(FilePermission.USR_RWX);
        permissionSet.remove(FilePermission.GRP_RWX);
        permissionSet.remove(FilePermission.OTH_RWX);
    }
}
