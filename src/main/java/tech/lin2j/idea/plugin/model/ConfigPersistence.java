package tech.lin2j.idea.plugin.model;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializer;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.OptionTag;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.ssh.SshServer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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

    private PluginSetting setting;

    private List<io.github.yueryou.easydev.plugin.model.Pipeline> pipelines;

    @Override
    public @Nullable ConfigPersistence getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ConfigPersistence state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public List<io.github.yueryou.easydev.plugin.model.Pipeline> getPipelines() {
        if (pipelines == null) {
            pipelines = new CopyOnWriteArrayList<>();
        }
        // 过滤 null 元素（可能由于 XML 反序列化失败导致）
        pipelines.removeIf(p -> p == null);
        checkUid(pipelines);
        return pipelines;
    }

    public void setPipelines(List<io.github.yueryou.easydev.plugin.model.Pipeline> pipelines) {
        this.pipelines = pipelines;
    }

    public List<SshServer> getSshServers() {
        if (sshServers == null) {
            sshServers = new CopyOnWriteArrayList<>();
        }
        checkUid(sshServers);
        return sshServers;
    }

    public void setSshServers(List<SshServer> sshServers) {
        this.sshServers = sshServers;
    }

    public List<Command> getCommands() {
        if (commands == null) {
            commands = new CopyOnWriteArrayList<>();
        }
        checkUid(commands);
        return commands;
    }

    public void setCommands(List<Command> commands) {
        this.commands = commands;
    }

    public List<UploadProfile> getUploadProfiles() {
        if (uploadProfiles == null) {
            uploadProfiles = new CopyOnWriteArrayList<>();
        }
        //
        int maxProfileId = uploadProfiles.stream().map(UploadProfile::getId)
                .filter(Objects::nonNull)
                .max(Integer::compareTo).orElse(0);
        for (UploadProfile profile : uploadProfiles) {
            if (profile.getId() == null) {
                profile.setId(++maxProfileId);
            }
        }
        checkUid(uploadProfiles);
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

    public PluginSetting getSetting() {
        if (setting == null) {
            setting = new PluginSetting();
        }
        return setting;
    }

    public void setSetting(PluginSetting setting) {
        this.setting = setting;
    }

    private void checkUid(List<? extends UniqueModel> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        list.stream()
            .filter(d -> d != null)  // 跳过 null 元素
            .forEach(d -> {
                if (d.getUid() == null) {
                    d.setUid(UUID.randomUUID().toString());
                }
            });
    }
}