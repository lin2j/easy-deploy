package tech.lin2j.idea.plugin.init;

import com.intellij.openapi.components.ApplicationComponent;
import tech.lin2j.idea.plugin.domain.model.Command;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.UploadProfile;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.uitl.SnowflakeUtil;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author linjinjia
 * @date 2024/4/27 21:10
 */
public class ModelIdInitializer implements ApplicationComponent {

    private final ReentrantLock lock = new ReentrantLock();

    public void init() {
        lock.lock();
        try {
            if (!requireUpdateModelId()) {
                return;
            }
            updateModelId();
            setUpdateCompleted();
        } finally {
            lock.unlock();
        }
    }

    private void updateModelId() {
        List<SshServer> allServers = ConfigHelper.sshServers();
        for (SshServer server : allServers) {
            long oldSshId = server.getId();
            long newSshId = getNewId();

            List<UploadProfile> profiles = ConfigHelper.getUploadProfileBySshId(oldSshId);
            for (UploadProfile profile : profiles) {
                long newProfileId = getNewId();
                profile.setId(newProfileId);
                profile.setSshId(newSshId);
            }

            List<Command> commands = ConfigHelper.getCommandsBySshId(oldSshId);
            for (Command command : commands) {
                long newCmdId = getNewId();
                command.setId(newCmdId);
                command.setSshId(newSshId);
            }

            server.setId(newSshId);
        }
    }

    private boolean requireUpdateModelId() {
        return true;
    }

    private void setUpdateCompleted() {

    }

    private long getNewId() {
        return SnowflakeUtil.getLongId();
    }
}