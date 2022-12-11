package tech.lin2j.idea.plugin.io;

import com.intellij.openapi.util.text.StringUtil;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author linjinjia
 * @date 2022/12/9 23:54
 */
public class ExtensionFilter implements FileFilter{
    private final String extensions;

    private final Set<String> extensionSet;

    public ExtensionFilter(String extensions) {
        if (StringUtil.isEmpty(extensions)) {
            this.extensions = "";
            this.extensionSet = new HashSet<>();
            return;
        }
        this.extensions = extensions;
        extensionSet =Arrays.stream(extensions.split(";"))
                .map(s -> s.replaceAll("\\*?\\.?", ""))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean accept(String f) {
        int dot = f.lastIndexOf('.');
        if (dot == -1) {
            return true;
        }
        String suffix = f.substring(dot + 1);
        return !extensionSet.contains(suffix);
    }

    @Override
    public boolean accept(File f) {
        return accept(f.getName());
    }

    public String getExtensions() {
        return extensions;
    }

    public Set<String> getExtensionSet() {
        return extensionSet;
    }
}