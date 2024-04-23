package icons;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.IconManager;

import javax.swing.Icon;

/**
 * @author linjinjia
 * @date 2022/4/29 15:51
 */
public interface MyIcons {
    private static Icon load(String path) {
        return IconManager.getInstance().getIcon(path, MyIcons.class);
    }

    Icon DEPLOY = IconLoader.getIcon("/icons/deploy.svg", MyIcons.class);

    Icon DEPLOY_DARK = IconLoader.getIcon("/icons/deploy_dark.svg", MyIcons.class);

    Icon TRANSFER = IconLoader.getIcon("/icons/transferToolWindow.svg", MyIcons.class);

    Icon TRANSFER_DARK = IconLoader.getIcon("/icons/transferToolWindow_dark.svg", MyIcons.class);

    Icon TAG = IconLoader.getIcon("/icons/tag.svg", MyIcons.class);
    Icon TAG_DARK = IconLoader.getIcon("/icons/tag_dark.svg", MyIcons.class);

    Icon SFTP = IconLoader.getIcon("/icons/sftp.svg", MyIcons.class);

    interface Actions {
        Icon SHOW_HIDDENS = load("/icons/actions/showHiddens.svg");
        Icon SHOW_HIDDENS_DARK = load("/icons/actions/showHiddens_dark.svg");
    }

    interface FileType {
        Icon VIDEO = load("/icons/fileTypes/video.svg");
        Icon DMG = load("/icons/fileTypes/dmg.svg");
        Icon C = load("/icons/fileTypes/c.svg");
        Icon C_DARK = load("/icons/fileTypes/c_dark.svg");
        Icon CPP = load("/icons/fileTypes/cpp.svg");
        Icon CPP_DARK = load("/icons/fileTypes/cpp_dark.svg");
        Icon WORD = load("/icons/fileTypes/word.svg");
        Icon EXCEl = load("/icons/fileTypes/excel.svg");
        Icon PPT = load("/icons/fileTypes/ppt.svg");
        Icon PDF = load("/icons/fileTypes/pdf.svg");
        Icon EBOOk = load("/icons/fileTypes/ebook.svg");
    }
}