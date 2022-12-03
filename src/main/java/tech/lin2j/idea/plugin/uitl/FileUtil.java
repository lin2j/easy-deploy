package tech.lin2j.idea.plugin.uitl;

import java.nio.file.Paths;

/**
 * @author linjinjia
 * @date 2022/12/3 15:05
 */
public class FileUtil {

    private FileUtil() {

    }

    /**
     * replace ~ file path symbol with the real user directory.
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

    public static String getHomeDir() {
        return String.valueOf(System.getProperty("user.home"));
    }
}