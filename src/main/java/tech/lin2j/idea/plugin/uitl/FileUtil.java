package tech.lin2j.idea.plugin.uitl;

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
     * @throws IOException IOException
     */
    public static String[] findAllFiles(String filepath) throws IOException {
        return null;
    }
}