package tech.lin2j.idea.plugin.uitl;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ui.PasswordInputUi;

import javax.annotation.Nullable;
import javax.swing.JOptionPane;

/**
 * @author linjinjia
 * @date 2022/7/1 22:08
 */
public class UiUtil {

    /**
     * get password from user input
     *
     * @return password
     */
    public static String requestPassword() {
        String[] password = new String[1];
        new PasswordInputUi(password).showAndGet();
        if (StringUtil.isEmpty(password[0])) {
            Messages.showErrorDialog("Password not found", "Error");
        }
        return password[0];
    }

    /**
     * request password if password not exists in server,
     * and return the clone of server
     *
     * @param server server information
     * @return the clone of server
     */
    public static SshServer requestPasswordIfNecessary(SshServer server) {
        SshServer ret = server.clone();
        if (StringUtil.isEmpty(ret.getPassword())) {
            ret.setPassword(requestPassword());
        }
        return ret;
    }

    /**
     * pop up an option pane when clicking the delete button
     *
     * @param specific specific message to show after the default message.
     *                 if it is empty, only the default message will be
     *                 displayed.
     * @return true if user confirms to do this action, or return false
     */
    public static boolean deleteConfirm(@Nullable String specific) {
        String defaultMessage = "Are you sure you want to remove the selected item ?";
        if (StringUtil.isNotEmpty(specific)) {
            defaultMessage = defaultMessage + "\n" + specific;
        }
        Object[] options = {"yes", "no"};
        int foo = JOptionPane.showOptionDialog(null,
                defaultMessage,
                "Warning",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);
        return foo == 0;
    }
}