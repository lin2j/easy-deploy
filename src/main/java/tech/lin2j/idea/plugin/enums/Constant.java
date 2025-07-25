package tech.lin2j.idea.plugin.enums;

import tech.lin2j.idea.plugin.model.UploadProfile;

/**
 * @author linjinjia
 * @date 2024/7/21 16:07
 */
public interface Constant {

    String EASY_DEPLOY = "EasyDeploy";

    String UPDATE_CHECKER_PROPERTY = "easy-deploy.statistics.timestamp";

    String STR_FALSE = "0";

    String STR_TRUE = "1";

    int INT_FALSE = 0;

    int INT_TRUE = 1;

    /**
     * default top inset of form item
     */
    int DEFAULT_TOP_INSET = 20;

    /**
     * The prefix of run tab title
     */
    String RUN_TAB_PREFIX = "ED@";

    /**
     * Separator of multiple local file paths
     */
    String LOCAL_FILE_SEPARATOR = "\n";

    /**
     * Path and identifier separation in {@link UploadProfile}
     */
    String LOCAL_FILE_INFO_SEPARATOR = "::";
}