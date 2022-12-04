package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.Icon;

/**
 * @author linjinjia
 * @date 2022/4/29 15:51
 */
public interface MyIcons {

    Icon DEPLOY = IconLoader.getIcon("/icons/deploy.svg", MyIcons.class);

    Icon DEPLOY_DARK = IconLoader.getIcon("/icons/deploy_dark.svg", MyIcons.class);
}