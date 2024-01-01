package tech.lin2j.idea.plugin.factory;

import tech.lin2j.idea.plugin.service.ISshService;
import tech.lin2j.idea.plugin.service.impl.JschSshService;
import tech.lin2j.idea.plugin.service.impl.SshService;

/**
 * @author linjinjia
 * @date 2023/12/21 22:40
 */
public class SshServiceFactory {
    /**
     * service instance
     */
    private static final ISshService SSH_INSTANCE = new SshService();

    private static final ISshService JSCH_INSTANCE = new JschSshService();

    public static ISshService getSshService() {
        return JSCH_INSTANCE;
    }
}