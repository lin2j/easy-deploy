package tech.lin2j.idea.plugin.domain.model;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.ssh.SshServer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author linjinjia
 * @date 2022/4/24 17:52
 */
@State(
        name = "SimpleDeployConfig",
        storages = @Storage(value = "deploy-helper-settings.xml")
)
public class ConfigPersistence implements PersistentStateComponent<ConfigPersistence>, Serializable {

    private List<SshServer> sshServers;

    private List<Command> commands;

    private List<UploadProfile> uploadProfiles;

    private List<String> serverTags;

    @Override
    public @Nullable ConfigPersistence getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ConfigPersistence state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public List<SshServer> getSshServers() {
        if (sshServers == null) {
            sshServers = new CopyOnWriteArrayList<>();
        }
        return sshServers;
    }

    public void setSshServers(List<SshServer> sshServers) {
        this.sshServers = sshServers;
    }

    public List<Command> getCommands() {
        if (commands == null) {
            commands = new CopyOnWriteArrayList<>();
        }
        return commands;
    }

    public void setCommands(List<Command> commands) {
        this.commands = commands;
    }

    public List<UploadProfile> getUploadProfiles() {
        if (uploadProfiles == null) {
            uploadProfiles = new CopyOnWriteArrayList<>();
        }
        return uploadProfiles;
    }

    public void setUploadProfiles(List<UploadProfile> uploadProfiles) {
        this.uploadProfiles = uploadProfiles;
    }

    public List<String> getServerTags() {
        if (serverTags == null) {
            serverTags = new ArrayList<>();
            serverTags.add("Default");
        }
        return serverTags;
    }

    public void setServerTags(List<String> serverTags) {
        this.serverTags = serverTags;
    }
}