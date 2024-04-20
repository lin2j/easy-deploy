package tech.lin2j.idea.plugin.file;

/**
 * @author linjinjia
 * @date 2024/4/18 06:37
 */
public class DirectoryInfo {

    private long size;
    private int files;
    private boolean isDirectory;
    private boolean isUpload;

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getFiles() {
        return files;
    }

    public void setFiles(int files) {
        this.files = files;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setUpload(boolean upload) {
        isUpload = upload;
    }
}