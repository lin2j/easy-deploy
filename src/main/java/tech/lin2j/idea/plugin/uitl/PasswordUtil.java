package tech.lin2j.idea.plugin.uitl;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ui.PasswordInputUi;

/**
 * @author linjinjia
 * @date 2022/7/1 22:08
 */
public class PasswordUtil {

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
}