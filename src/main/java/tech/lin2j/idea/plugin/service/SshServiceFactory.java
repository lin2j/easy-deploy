package tech.lin2j.idea.plugin.service;

import tech.lin2j.idea.plugin.service.impl.SshService;

/**
 * @author linjinjia
 * @date 2023/12/21 22:40
 */
public class SshServiceFactory {
    /**
     * service instance
     */
    private static final SshService INSTANCE = new SshService();

    public static ISshService getSshService() {
        return INSTANCE;
    }
}