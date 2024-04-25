package tech.lin2j.idea.plugin.uitl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

/**
 * Special thanks to [https://github.com/houxinlin/cool-request]
 *
 * @author linjinjia
 * @date 2024/4/25 22:35
 */
public class WebBrowseUtil {

    private static final Logger log = Logger.getInstance(WebBrowseUtil.class);

    public static void browse(String url) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception e) {
            nativeCommand(url);
        }
    }

    public static void nativeCommand(final String uri) {
        try {
            if (SystemInfo.isMac) {
                Runtime.getRuntime().exec(new String[]{"open", uri});
            } else if (SystemInfo.isWindows) {
                Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", uri});
            } else { //assume Unix or Linux
                new Thread(() -> {
                    try {
                        for (String[] browser : commands) {
                            try {
                                String[] command = new String[browser.length];
                                for (int i = 0; i < browser.length; i++)
                                    if (browser[i].equals("$1"))
                                        command[i] = uri;
                                    else
                                        command[i] = browser[i];
                                if (Runtime.getRuntime().exec(command).waitFor() == 0)
                                    return;
                            } catch (IOException ignored) {
                            }
                        }
                        String browsers = System.getenv("BROWSER") == null ? "x-www-browser:firefox:iceweasel:seamonkey:mozilla:" +
                                "epiphany:konqueror:chromium:chromium-browser:google-chrome:" +
                                "www-browser:links2:elinks:links:lynx:w3m" : System.getenv("BROWSER");
                        for (String browser : browsers.split(":")) {
                            try {
                                Runtime.getRuntime().exec(new String[]{browser, uri});
                                return;
                            } catch (IOException ignored) {
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }).start();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static final String[][] commands = new String[][]{
            {"xdg-open", "$1"},
            {"gio", "open", "$1"},
            {"gvfs-open", "$1"},
            {"gnome-open", "$1"},  // Gnome
            {"mate-open", "$1"},  // Mate
            {"exo-open", "$1"},  // Xfce
            {"enlightenment_open", "$1"},  // Enlightenment
            {"gdbus", "call", "--session", "--dest", "org.freedesktop.portal.Desktop",
                    "--object-path", "/org/freedesktop/portal/desktop",
                    "--method", "org.freedesktop.portal.OpenURI.OpenURI",
                    "", "$1", "{}"},  // Flatpak
            {"open", "$1"},  // Mac OS fallback
            {"rundll32", "url.dll,FileProtocolHandler", "$1"},  // Windows fallback
    };
}