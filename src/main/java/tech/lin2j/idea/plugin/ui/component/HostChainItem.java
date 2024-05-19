package tech.lin2j.idea.plugin.ui.component;

import tech.lin2j.idea.plugin.ssh.SshServer;

/**
 * @author linjinjia
 * @date 2024/5/19 11:42
 */
public class HostChainItem {

    private SshServer server;

    /**
     * the dependencies of some of host from a cycle
     */
    private Boolean cycle;

    public HostChainItem(SshServer server, Boolean cycle) {
        this.server = server;
        this.cycle = cycle;
    }

    public SshServer getServer() {
        return server;
    }

    public Boolean isCycle() {
        return cycle;
    }

    public void setServer(SshServer server) {
        this.server = server;
    }

    public void setCycle(Boolean cycle) {
        this.cycle = cycle;
    }
}