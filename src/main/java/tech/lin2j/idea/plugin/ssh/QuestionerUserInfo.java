package tech.lin2j.idea.plugin.ssh;

import com.jcraft.jsch.UserInfo;
import com.jediterm.terminal.Questioner;

/**
 * @author linjinjia
 * @date 2022/6/25 22:51
 */
public class QuestionerUserInfo implements UserInfo {

    private Questioner questioner;

    private String password;

    private String passPhrase;

    public QuestionerUserInfo(Questioner questioner) {
        this.questioner = questioner;
    }

    @Override
    public String getPassphrase() {
        return passPhrase;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean promptPassword(String message) {
        password = questioner.questionHidden(message + ":");
        return true;
    }

    @Override
    public boolean promptPassphrase(String message) {
        passPhrase = questioner.questionHidden(message + ":");
        return true;
    }

    @Override
    public boolean promptYesNo(String message) {
        String yn = questioner.questionVisible(message + " [Y/N]:", "Y");
        String lyn = yn.toLowerCase();
        return "y".equals(lyn) || "yes".equals(lyn);
    }

    @Override
    public void showMessage(String message) {
        questioner.showMessage(message);
    }
}