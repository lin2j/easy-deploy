package tech.lin2j.idea.plugin.uitl;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.table.JBTable;
import tech.lin2j.idea.plugin.enums.AuthType;
import tech.lin2j.idea.plugin.ssh.SshServer;

import javax.annotation.Nullable;
import javax.swing.JOptionPane;
import java.awt.Toolkit;

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
        String tip = MessagesBundle.getText("dialog.password.tip");
        String title = MessagesBundle.getText("dialog.password.frame");
        String password = Messages.showPasswordDialog(tip, title);

        if (StringUtil.isEmpty(password)) {
            Messages.showErrorDialog(MessagesBundle.getText("dialog.password.error"), "Error");
        }
        return password;
    }

    /**
     * request password if password not exists in server,
     * and return the clone of server
     *
     * @param server server information
     * @return the clone of server
     */
    public static SshServer requestPasswordIfNecessary(SshServer server) {
        Integer authType = server.getAuthType();
        SshServer ret = server.clone();
        if (AuthType.needPassword(authType)
                && StringUtil.isEmpty(ret.getPassword())) {
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
        String defaultMessage = "Are you sure you want to remove the selected item?";
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

    public static int screenWidth() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        return toolkit.getScreenSize().width;
    }

    public static void hideTableLine(JBTable table) {
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
    }
}