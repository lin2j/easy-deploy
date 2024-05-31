package tech.lin2j.idea.plugin.enums;

import java.util.Arrays;
import java.util.List;

/**
 * @author linjinjia
 * @date 2024/5/25 10:12
 */
public enum SFTPAction {

    NONE, Transfer, Properties;

    public static List<SFTPAction> asList() {
        return Arrays.asList(SFTPAction.values());
    }
}