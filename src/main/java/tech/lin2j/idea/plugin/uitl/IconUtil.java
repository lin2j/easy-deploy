package tech.lin2j.idea.plugin.uitl;

import com.intellij.ide.TypePresentationService;
import com.intellij.openapi.fileTypes.DirectoryFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PlatformIcons;

import javax.swing.Icon;

/**
 * @author linjinjia
 * @date 2024/3/31 21:21
 */
public class IconUtil {

    public static Icon getIcon(VirtualFile vFile) {
        Icon icon = TypePresentationService.getService().getIcon(vFile);
        if (icon != null) {
            return icon;
        }
        FileType fileType = vFile.getFileType();
        if (vFile.isDirectory() && !(fileType instanceof DirectoryFileType)) {
            return PlatformIcons.FOLDER_ICON;
        }
        return fileType.getIcon();
    }
}