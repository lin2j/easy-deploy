package tech.lin2j.idea.plugin.file;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.time.DateFormatUtils;

import javax.swing.Icon;

/**
 * @author linjinjia
 * @date 2024/4/6 10:18
 */
public class LocalTableFile implements TableFile {
    private final VirtualFile vf;

    public LocalTableFile(VirtualFile vf) {
        this.vf = vf;
    }

    @Override
    public Icon getIcon() {
        vf.isRecursiveOrCircularSymLink();
        return vf.isDirectory() ? AllIcons.Nodes.Folder : vf.getFileType().getIcon();
    }

    @Override
    public String getName() {
        return vf.getName();
    }

    @Override
    public String getSize() {
        return StringUtil.formatFileSize(vf.getLength());
    }

    @Override
    public String getType() {
        return vf.isDirectory() ? "Folder" : vf.getFileType().getName();
    }

    @Override
    public String getModified() {
        return DateFormatUtils.format(vf.getTimeStamp(), "yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public boolean isDirectory() {
        return vf.isDirectory();
    }

    @Override
    public String getFilePath() {
        return vf.getPath();
    }

    @Override
    public String getParent() {
        VirtualFile parent = vf.getParent();
        return parent == null ? null : parent.getPath();
    }
}