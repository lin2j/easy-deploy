package tech.lin2j.idea.plugin.factory;

import tech.lin2j.idea.plugin.service.ISshService;
import tech.lin2j.idea.plugin.service.impl.SshjSshService;

/**
 * @author linjinjia
 * @date 2023/12/21 22:40
 */
public class SshServiceFactory {

    private static final ISshService INSTANCE = new SshjSshService();

    public static ISshService getSshService() {
        return INSTANCE;
    }
}