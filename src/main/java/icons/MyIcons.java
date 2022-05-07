package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.Icon;

/**
 * @author linjinjia
 * @date 2022/4/29 15:51
 */
public interface MyIcons {

    Icon DEPLOY_LIGHT = IconLoader.getIcon("/icons/deploy_light.svg", MyIcons.class);

    Icon DEPLOY_DARK = IconLoader.getIcon("/icons/deploy_dark.svg", MyIcons.class);

    Icon DEPLOY_BLUE = IconLoader.getIcon("/icons/deploy_blue.svg", MyIcons.class);

    Icon MESSAGE_BLUE = IconLoader.getIcon("/icons/message_blue.svg", MyIcons.class);


}