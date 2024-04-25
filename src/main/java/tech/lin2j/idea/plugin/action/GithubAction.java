package tech.lin2j.idea.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import icons.MyIcons;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.uitl.WebBrowseUtil;

/**
 * @author linjinjia
 * @date 2024/4/25 22:43
 */
public class GithubAction extends AnAction {
    public GithubAction() {
        super("Github", "Plugin github repository", MyIcons.Actions.Github);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        WebBrowseUtil.browse("https://github.com/lin2j/easy-deploy");
    }
}