package io.github.yueryou.easydev.plugin.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 变量解析器，支持 {{varName}} 语法
 */
public class VariableResolver {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    public VariableResolver() {
    }

    /**
     * 解析模板中的变量
     *
     * @param template  模板字符串
     * @param variables 变量映射表
     * @return 解析后的字符串
     */
    public static String resolve(String template, Map<String, Object> variables) {
        if (template == null || template.isEmpty()) {
            return template;
        }
        if (variables == null || variables.isEmpty()) {
            return template;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String varName = matcher.group(1).trim();
            Object value = variables.get(varName);
            String replacement = (value != null) ? value.toString() : matcher.group(0);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 检查模板是否包含指定变量
     *
     * @param template 模板字符串
     * @param varName  变量名
     * @return 如果包含返回 true，否则返回 false
     */
    public static boolean containsVariable(String template, String varName) {
        if (template == null || template.isEmpty()) {
            return false;
        }
        if (varName == null || varName.isEmpty()) {
            return false;
        }

        Pattern pattern = Pattern.compile("\\{\\{\\s*" + Pattern.quote(varName) + "\\s*\\}\\}");
        return pattern.matcher(template).find();
    }
}
