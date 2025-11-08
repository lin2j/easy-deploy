package tech.lin2j.idea.plugin.file.filter;

import com.intellij.openapi.util.text.StringUtil;
import tech.lin2j.idea.plugin.ssh.CommandLog;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author linjinjia
 * @date 2022/12/9 23:54
 */
public class ExtExcludeFilter implements FileFilter {
    private final String extensions;
    private final Set<Pattern> extensionPatternSet;
    private final CommandLog commandLog;

    public ExtExcludeFilter(String extensions, CommandLog commandLog) {
        this.commandLog = commandLog;
        if (StringUtil.isEmpty(extensions)) {
            this.extensions = "";
            this.extensionPatternSet = new HashSet<>();
            return;
        }
        this.extensions = extensions;
        extensionPatternSet = Arrays.stream(extensions.split(";"))
                .map(regx -> {
                    String pattern = fillPattern(regx);
                    return Pattern.compile(pattern);
                })
                .collect(Collectors.toSet());
    }

    public static String fillPattern(String pattern) {
        return "^" + pattern
                .replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".")
                .replace("+", "\\+")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("{", "\\{")
                .replace("}", "\\}") + "$";
    }

    @Override
    public boolean accept(String f) {
        if (StringUtil.isEmpty(extensions)) {
            return true;
        }
        // 使用正则表达式验证
        for (Pattern pattern : extensionPatternSet) {
            Matcher m = pattern.matcher(f);
            if (m.matches()) {
                commandLog.info("[" + f + "] excluded by '" + extensions + "'");
                return false;
            }
        }
        return true;
    }

    public String getExtensions() {
        return extensions;
    }

    public Set<Pattern> getExtensionSet() {
        return extensionPatternSet;
    }
}
