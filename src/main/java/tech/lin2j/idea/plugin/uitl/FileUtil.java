package tech.lin2j.idea.plugin.uitl;

import com.intellij.ide.highlighter.ArchiveFileType;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.fileTypes.UnknownFileType;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.SystemProperties;
import tech.lin2j.idea.plugin.file.DirectoryInfo;
import tech.lin2j.idea.plugin.file.PluginFileTypeRegistry;
import tech.lin2j.idea.plugin.file.fileTypes.SpecifiedArchiveFileType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author linjinjia
 * @date 2022/12/3 15:05
 */
public class FileUtil {

    private FileUtil() {

    }

    /**
     * Check if the given path is a directory
     *
     * @param file file name
     * @return <code>true</code> if and only if the file denoted by this
     * abstract pathname exists <em>and</em> is a directory;
     * <code>false</code> otherwise
     */
    public static boolean isDirectory(String file) {
        boolean isDir = false;
        try {
            isDir = new File(file).isDirectory();
        } catch (Exception ignore) {

        }
        return isDir;
    }

    /**
     * Replace ~ file path symbol with the real user directory.
     * if filepath starts with "~", then replace "~" it
     *
     * @param filepath file path
     * @return real path of filepath
     */
    public static String replaceHomeSymbol(String filepath) {
        String newPath = filepath;
        String home = getHomeDir();
        if (filepath.startsWith("~")) {
            newPath = Paths.get(home, filepath.substring(1)).toString();
        }
        return newPath;
    }

    /**
     * Retrieve the home directory of the current user
     *
     * @return directory
     */
    public static String getHomeDir() {
        return String.valueOf(System.getProperty("user.home"));
    }

    /**
     * Returns an array of strings naming the files and directories in the
     * directory denoted by filepath, if filepath is represent a directory,
     * this array of strings will be empty if the directory is empty.
     * if filepath is a file, the return itself
     *
     * @param filepath absolute file path,
     *                 represent file or directory
     * @return array of strings naming the file and directories in the directory
     */
    public static String[] findAllFiles(String filepath) {
        return null;
    }

    public static DirectoryInfo calcDirectorySize(String directory) {
        return calcDirectorySize(new File(directory));
    }

    /**
     * Calculates the size of a directory recursively.
     *
     * @param directory The directory whose size needs to be calculated.
     * @return The total size of the directory and its contents, in bytes.
     */
    public static DirectoryInfo calcDirectorySize(File directory) {
        DirectoryInfo di = new DirectoryInfo();
        // If the given file object is not a directory, return its size
        if (!directory.isDirectory()) {
            di.setDirectory(false);
            di.setFiles(1);
            di.setSize(directory.length());
            return di;
        }

        di.setDirectory(true);
        // Traverse all files and subdirectories in the directory and accumulate their sizes
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                DirectoryInfo subDi = calcDirectorySize(file);
                di.setSize(di.getSize() + subDi.getSize());
                di.setFiles(di.getFiles() + subDi.getFiles());
            }
        }

        return di;
    }

    public static FileType getFileType(String filename) {
        FileType fileType = FileTypeRegistry.getInstance().getFileTypeByFileName(filename);

        String ext = getExtension(filename);
        if (fileType instanceof ArchiveFileType) {
            fileType = new SpecifiedArchiveFileType(fileType, ext);
        }

        if (fileType instanceof UnknownFileType) {
            fileType = PluginFileTypeRegistry.getFileTypeByExtension(ext);
        }

        return fileType;
    }

    public static String getExtension(String filename) {
        int index = filename.lastIndexOf('.');
        if (index < 0) return "";
        return filename.substring(index + 1);
    }

    public static void openDir(String targetPath) throws IOException{
        if (SystemInfo.isMac) {
            Runtime.getRuntime().exec("open " + targetPath);
        } else if (SystemInfo.isWindows) {
            Runtime.getRuntime().exec("cmd /c start " + targetPath);
        }
    }

    public static VirtualFile getHome() {
        return LocalFileSystem.getInstance().findFileByIoFile(new File(SystemProperties.getUserHome()));
    }


    public static VirtualFile virtualFile(String filepath) {
        return LocalFileSystem.getInstance().findFileByIoFile(new File(filepath));
    }
}