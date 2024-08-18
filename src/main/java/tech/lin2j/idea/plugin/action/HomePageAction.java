package tech.lin2j.idea.plugin.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import icons.MyIcons;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;
import tech.lin2j.idea.plugin.uitl.WebBrowseUtil;

/**
 * @author linjinjia
 * @date 2024/4/25 22:43
 */
public class HomePageAction extends NewUpdateThreadAction {
    private static final String text = MessagesBundle.getText("action.dashboard.home-page.text");

    public HomePageAction() {
        super(text, "Plugin Home page", MyIcons.Actions.HomePage);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        WebBrowseUtil.browse("https://lin2j.tech/md/easy-deploy/brief.html");
    }
}