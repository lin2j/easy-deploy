package tech.lin2j.idea.plugin.model;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.util.xmlb.XmlSerializer;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.ssh.SshServer;

import java.io.IOException;
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
public class ConfigPersistence implements PersistentStateComponent<ConfigPersistence>, JDOMExternalizable, Serializable {

    private List<SshServer> sshServers;

    private List<Command> commands;

    private List<UploadProfile> uploadProfiles;

    private List<String> serverTags;

    private PluginSetting setting;

    private List<io.github.yueryou.easydev.plugin.model.Pipeline> pipelines;

    @Override
    public void writeExternal(Element element) {
        // 序列化 sshServers
        if (sshServers != null && !sshServers.isEmpty()) {
            Element serversElement = new Element("sshServers");
            for (SshServer server : sshServers) {
                Element serverElement = new Element("server");
                serverElement.setAttribute("id", server.getId() != null ? String.valueOf(server.getId()) : "");
                serverElement.setAttribute("uid", server.getUid() != null ? server.getUid() : "");
                serverElement.setAttribute("ip", server.getIp() != null ? server.getIp() : "");
                serverElement.setAttribute("port", String.valueOf(server.getPort()));
                serverElement.setAttribute("username", server.getUsername() != null ? server.getUsername() : "");
                serverElement.setAttribute("authType", String.valueOf(server.getAuthType()));
                serverElement.setAttribute("tag", server.getTag() != null ? server.getTag() : "");
                serverElement.setAttribute("description", server.getDescription() != null ? server.getDescription() : "");
                serversElement.addContent(serverElement);
            }
            element.addContent(serversElement);
        }

        // 序列化 commands
        if (commands != null && !commands.isEmpty()) {
            Element commandsElement = new Element("commands");
            for (Command cmd : commands) {
                Element cmdElement = new Element("command");
                cmdElement.setAttribute("id", cmd.getId() != null ? String.valueOf(cmd.getId()) : "");
                cmdElement.setAttribute("uid", cmd.getUid() != null ? cmd.getUid() : "");
                cmdElement.setAttribute("title", cmd.getTitle() != null ? cmd.getTitle() : "");
                cmdElement.setAttribute("content", cmd.getContent() != null ? cmd.getContent() : "");
                cmdElement.setAttribute("dir", cmd.getDir() != null ? cmd.getDir() : "");
                cmdElement.setAttribute("sshId", cmd.getSshId() != null ? String.valueOf(cmd.getSshId()) : "");
                commandsElement.addContent(cmdElement);
            }
            element.addContent(commandsElement);
        }

        // 序列化 uploadProfiles
        if (uploadProfiles != null && !uploadProfiles.isEmpty()) {
            Element profilesElement = new Element("uploadProfiles");
            for (UploadProfile profile : uploadProfiles) {
                Element profileElement = new Element("profile");
                profileElement.setAttribute("id", profile.getId() != null ? String.valueOf(profile.getId()) : "");
                profileElement.setAttribute("uid", profile.getUid() != null ? profile.getUid() : "");
                profileElement.setAttribute("name", profile.getName() != null ? profile.getName() : "");
                profileElement.setAttribute("file", profile.getFile() != null ? profile.getFile() : "");
                profileElement.setAttribute("location", profile.getLocation() != null ? profile.getLocation() : "");
                profileElement.setAttribute("exclude", profile.getExclude() != null ? profile.getExclude() : "");
                profileElement.setAttribute("sshId", profile.getSshId() != null ? String.valueOf(profile.getSshId()) : "");
                profileElement.setAttribute("preCommandId", profile.getPreCommandId() != null ? String.valueOf(profile.getPreCommandId()) : "");
                profileElement.setAttribute("postCommandId", profile.getPostCommandId() != null ? String.valueOf(profile.getPostCommandId()) : "");
                profileElement.setAttribute("useUploadPath", profile.getUseUploadPath() != null ? String.valueOf(profile.getUseUploadPath()) : "false");
                profileElement.setAttribute("includeCurrentDir", profile.getIncludeCurrentDir() != null ? String.valueOf(profile.getIncludeCurrentDir()) : "false");
                profilesElement.addContent(profileElement);
            }
            element.addContent(profilesElement);
        }

        // 序列化 serverTags
        if (serverTags != null && !serverTags.isEmpty()) {
            Element tagsElement = new Element("serverTags");
            for (String tag : serverTags) {
                Element tagElement = new Element("tag");
                tagElement.setText(tag);
                tagsElement.addContent(tagElement);
            }
            element.addContent(tagsElement);
        }

        // 序列化 setting
        if (setting != null) {
            Element settingElement = new Element("setting");
            settingElement.setAttribute("uploadProgressColor", setting.getUploadProgressColor() != null ? setting.getUploadProgressColor() : "");
            settingElement.setAttribute("downloadProgressColor", setting.getDownloadProgressColor() != null ? setting.getDownloadProgressColor() : "");
            element.addContent(settingElement);
        }

        // 序列化 pipelines - 使用 Pipeline 自身的 JDOMExternalizable 实现
        if (pipelines != null && !pipelines.isEmpty()) {
            Element pipelinesElement = new Element("pipelines");
            for (io.github.yueryou.easydev.plugin.model.Pipeline pipeline : pipelines) {
                Element pipelineElement = new Element("pipeline");
                pipeline.writeExternal(pipelineElement);
                pipelinesElement.addContent(pipelineElement);
            }
            element.addContent(pipelinesElement);
        }
    }

    @Override
    public void readExternal(Element element) {
        // 反序列化 sshServers
        List<SshServer> servers = new ArrayList<>();
        Element serversElement = element.getChild("sshServers");
        if (serversElement != null) {
            for (Element serverElement : serversElement.getChildren("server")) {
                SshServer server = new SshServer();
                String idStr = serverElement.getAttributeValue("id");
                if (idStr != null && !idStr.isEmpty()) {
                    server.setId(Integer.parseInt(idStr));
                }
                server.setUid(serverElement.getAttributeValue("uid"));
                server.setIp(serverElement.getAttributeValue("ip"));
                server.setPort(Integer.parseInt(serverElement.getAttributeValue("port")));
                server.setUsername(serverElement.getAttributeValue("username"));
                String authTypeStr = serverElement.getAttributeValue("authType");
                if (authTypeStr != null && !authTypeStr.isEmpty()) {
                    server.setAuthType(Integer.parseInt(authTypeStr));
                }
                server.setTag(serverElement.getAttributeValue("tag"));
                server.setDescription(serverElement.getAttributeValue("description"));
                servers.add(server);
            }
        }
        sshServers = new CopyOnWriteArrayList<>(servers);

        // 反序列化 commands
        List<Command> cmds = new ArrayList<>();
        Element commandsElement = element.getChild("commands");
        if (commandsElement != null) {
            for (Element cmdElement : commandsElement.getChildren("command")) {
                Command cmd = new Command();
                String idStr = cmdElement.getAttributeValue("id");
                if (idStr != null && !idStr.isEmpty()) {
                    cmd.setId(Integer.parseInt(idStr));
                }
                cmd.setUid(cmdElement.getAttributeValue("uid"));
                cmd.setTitle(cmdElement.getAttributeValue("title"));
                cmd.setContent(cmdElement.getAttributeValue("content"));
                cmd.setDir(cmdElement.getAttributeValue("dir"));
                String sshIdStr = cmdElement.getAttributeValue("sshId");
                if (sshIdStr != null && !sshIdStr.isEmpty()) {
                    cmd.setSshId(Integer.parseInt(sshIdStr));
                }
                cmds.add(cmd);
            }
        }
        commands = new CopyOnWriteArrayList<>(cmds);

        // 反序列化 uploadProfiles
        List<UploadProfile> profiles = new ArrayList<>();
        Element profilesElement = element.getChild("uploadProfiles");
        if (profilesElement != null) {
            for (Element profileElement : profilesElement.getChildren("profile")) {
                UploadProfile profile = new UploadProfile();
                String idStr = profileElement.getAttributeValue("id");
                if (idStr != null && !idStr.isEmpty()) {
                    profile.setId(Integer.parseInt(idStr));
                }
                profile.setUid(profileElement.getAttributeValue("uid"));
                profile.setName(profileElement.getAttributeValue("name"));
                profile.setFile(profileElement.getAttributeValue("file"));
                profile.setLocation(profileElement.getAttributeValue("location"));
                profile.setExclude(profileElement.getAttributeValue("exclude"));
                String sshIdStr = profileElement.getAttributeValue("sshId");
                if (sshIdStr != null && !sshIdStr.isEmpty()) {
                    profile.setSshId(Integer.parseInt(sshIdStr));
                }
                String preCmdIdStr = profileElement.getAttributeValue("preCommandId");
                if (preCmdIdStr != null && !preCmdIdStr.isEmpty()) {
                    profile.setPreCommandId(Integer.parseInt(preCmdIdStr));
                }
                String postCmdIdStr = profileElement.getAttributeValue("postCommandId");
                if (postCmdIdStr != null && !postCmdIdStr.isEmpty()) {
                    profile.setPostCommandId(Integer.parseInt(postCmdIdStr));
                }
                String useUploadPathStr = profileElement.getAttributeValue("useUploadPath");
                if (useUploadPathStr != null && !useUploadPathStr.isEmpty()) {
                    profile.setUseUploadPath(Boolean.parseBoolean(useUploadPathStr));
                }
                String includeCurrentDirStr = profileElement.getAttributeValue("includeCurrentDir");
                if (includeCurrentDirStr != null && !includeCurrentDirStr.isEmpty()) {
                    profile.setIncludeCurrentDir(Boolean.parseBoolean(includeCurrentDirStr));
                }
                profiles.add(profile);
            }
        }
        uploadProfiles = new CopyOnWriteArrayList<>(profiles);

        // 反序列化 serverTags
        List<String> tags = new ArrayList<>();
        Element tagsElement = element.getChild("serverTags");
        if (tagsElement != null) {
            for (Element tagElement : tagsElement.getChildren("tag")) {
                tags.add(tagElement.getText());
            }
        }
        serverTags = tags;

        // 反序列化 setting
        Element settingElement = element.getChild("setting");
        if (settingElement != null) {
            setting = new PluginSetting();
            setting.setUploadProgressColor(settingElement.getAttributeValue("uploadProgressColor"));
            setting.setDownloadProgressColor(settingElement.getAttributeValue("downloadProgressColor"));
        }

        // 反序列化 pipelines
        List<io.github.yueryou.easydev.plugin.model.Pipeline> pls = new ArrayList<>();
        Element pipelinesElement = element.getChild("pipelines");
        if (pipelinesElement != null) {
            for (Element pipelineElement : pipelinesElement.getChildren("pipeline")) {
                io.github.yueryou.easydev.plugin.model.Pipeline pipeline = new io.github.yueryou.easydev.plugin.model.Pipeline();
                pipeline.readExternal(pipelineElement);
                pls.add(pipeline);
            }
        }
        pipelines = new CopyOnWriteArrayList<>(pls);
    }

    @Override
    public @Nullable ConfigPersistence getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ConfigPersistence state) {
        // 使用 JDOMExternalizable 序列化，此方法不会被调用
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