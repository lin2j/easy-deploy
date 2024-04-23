package tech.lin2j.idea.plugin.domain.model.event;

import tech.lin2j.idea.plugin.event.ApplicationEvent;
import tech.lin2j.idea.plugin.ssh.SshServer;

import java.util.List;

/**
 * @author linjinjia
 * @date 2022/4/27 10:28
 */
public class TableRefreshEvent extends ApplicationEvent {

    private boolean tagRefresh;

    List<SshServer> sshServers;

    public TableRefreshEvent(List<SshServer> sshServers) {
        super(new Object());
        this.sshServers = sshServers;
    }

    public TableRefreshEvent() {
        super(new Object());
    }

    public List<SshServer> getSshServers() {
        return sshServers;
    }

    public boolean isTagRefresh() {
        return tagRefresh;
    }

    public void setTagRefresh(boolean tagRefresh) {
        this.tagRefresh = tagRefresh;
    }
}