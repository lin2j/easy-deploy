package icons;

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

    Icon Deploy = IconLoader.getIcon("/icons/deploy.svg", MyIcons.class);
    Icon DeployDark = IconLoader.getIcon("/icons/deploy_dark.svg", MyIcons.class);

    Icon Transfer = IconLoader.getIcon("/icons/transferToolWindow.svg", MyIcons.class);
    Icon TransferDark = IconLoader.getIcon("/icons/transferToolWindow_dark.svg", MyIcons.class);

    Icon Tag = IconLoader.getIcon("/icons/tag.svg", MyIcons.class);
    Icon TagDark = IconLoader.getIcon("/icons/tag_dark.svg", MyIcons.class);

    Icon SFTP = IconLoader.getIcon("/icons/sftp.svg", MyIcons.class);
    Icon UploadProfile = IconLoader.getIcon("/icons/upload_profile.svg", MyIcons.class);
    Icon EasyDeploy = IconLoader.getIcon("/icons/easy-deploy.svg", MyIcons.class);

    interface Actions {
        Icon showHidden = load("/icons/actions/showHiddens.svg");
        Icon showHiddenDark = load("/icons/actions/showHiddens_dark.svg");
        Icon AddHost = IconLoader.getIcon("/icons/actions/host.svg", MyIcons.class);
        Icon Settings = IconLoader.getIcon("/icons/actions/my_settings.svg", MyIcons.class);
        Icon Refresh = IconLoader.getIcon("/icons/actions/my_refresh.svg", MyIcons.class);
        Icon Github = IconLoader.getIcon("/icons/actions/github.svg", MyIcons.class);
        Icon HomePage = IconLoader.getIcon("/icons/actions/home-page.svg", MyIcons.class);
        Icon Copy = IconLoader.getIcon("/icons/actions/copy.svg", MyIcons.class);
        Icon Paste = IconLoader.getIcon("/icons/actions/paste.svg", MyIcons.class);
        Icon Add = IconLoader.getIcon("/icons/actions/my-add.svg", MyIcons.class);
        Icon ExportAndImport = IconLoader.getIcon("/icons/actions/export_import.svg", MyIcons.class);
        Icon Export = IconLoader.getIcon("/icons/actions/my_export.svg", MyIcons.class);
        Icon Import = IconLoader.getIcon("/icons/actions/my_import.svg", MyIcons.class);
        Icon Clean = IconLoader.getIcon("/icons/actions/my_clean.svg", MyIcons.class);
        Icon Share = IconLoader.getIcon("/icons/actions/my_share.svg", MyIcons.class);
        Icon Shared = IconLoader.getIcon("/icons/actions/my_share_selected.svg", MyIcons.class);
    }

    interface FileType {
        Icon Video = load("/icons/fileTypes/video.svg");
        Icon Dmg = load("/icons/fileTypes/dmg.svg");
        Icon C = load("/icons/fileTypes/c.svg");
        Icon CDark = load("/icons/fileTypes/c_dark.svg");
        Icon CPP = load("/icons/fileTypes/cpp.svg");
        Icon CPPDark = load("/icons/fileTypes/cpp_dark.svg");
        Icon Word = load("/icons/fileTypes/word.svg");
        Icon Excel = load("/icons/fileTypes/excel.svg");
        Icon PPT = load("/icons/fileTypes/ppt.svg");
        Icon PDF = load("/icons/fileTypes/pdf.svg");
        Icon EBook = load("/icons/fileTypes/ebook.svg");
        Icon Log = load("/icons/fileTypes/log.svg");
    }
}