package tech.lin2j.idea.plugin.model;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import tech.lin2j.idea.plugin.enums.TransferMode;
import tech.lin2j.idea.plugin.ssh.SshServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author linjinjia
 * @date 2022/4/25 17:27
 */
public class ConfigHelper {
    // 禁止实例化
    private ConfigHelper() {
        throw new IllegalStateException("Utility class");
    }
    private static final Logger log = Logger.getInstance(ConfigHelper.class);

    private static volatile ConfigPersistence CONFIG_PERSISTENCE = null;

    private static Map<Integer, SshServer> SSH_SERVER_MAP;

    private static List<Command> COMMAND_LIST;

    private static Map<Integer, List<Command>> COMMAND_MAP;

    private static Map<Integer, List<UploadProfile>> UPLOAD_PROFILE_MAP;

    public static void ensureConfigLoadInMemory() {
        if (CONFIG_PERSISTENCE == null) {
            synchronized (ConfigHelper.class) {
                if (CONFIG_PERSISTENCE == null) {
                    log.info("Easy Dev first time load configuration");
                    CONFIG_PERSISTENCE = ApplicationManager.getApplication().getService(ConfigPersistence.class);
                    refreshConfig();
                    log.info("Easy Dev first time load configuration finished");
                }
            }
        }
    }

    public static List<Command> getCommandList() {
        ensureConfigLoadInMemory();
        return COMMAND_LIST;
    }

    public static void refreshConfig() {
        SSH_SERVER_MAP = CONFIG_PERSISTENCE.getSshServers().stream()
                .collect(Collectors.toMap(SshServer::getId, s -> s, (s1, s2) -> s1));

        COMMAND_LIST = CONFIG_PERSISTENCE.getCommands();

        COMMAND_MAP = COMMAND_LIST.stream()
                .filter(it -> Objects.nonNull(it.getSshId()))
                .collect(Collectors.groupingBy(Command::getSshId));

        UPLOAD_PROFILE_MAP = CONFIG_PERSISTENCE.getUploadProfiles().stream()
                .collect(Collectors.groupingBy(UploadProfile::getSshId));
    }

    public static void cleanConfig() {
        CONFIG_PERSISTENCE.setSshServers(null);
        CONFIG_PERSISTENCE.setCommands(null);
        CONFIG_PERSISTENCE.setUploadProfiles(null);
        CONFIG_PERSISTENCE.setServerTags(null);

        refreshConfig();
    }

    // api

    public static int language() {
        ensureConfigLoadInMemory();
        return pluginSetting().getI18nType();
    }

    public static int transferMode() {
        ensureConfigLoadInMemory();
        return pluginSetting().getTransferMode();
    }

    public static boolean isSCPTransferMode() {
        ensureConfigLoadInMemory();
        return Objects.equals(pluginSetting().getTransferMode(), TransferMode.SCP.getType());
    }

    public static SshServer getSshServerById(int id) {
        ensureConfigLoadInMemory();
        return SSH_SERVER_MAP.get(id);
    }

    public static List<SshServer> sshServers() {
        ensureConfigLoadInMemory();
        return CONFIG_PERSISTENCE.getSshServers();
    }

    public static int maxSshServerId() {
        ensureConfigLoadInMemory();
        return CONFIG_PERSISTENCE.getSshServers().stream()
                .map(SshServer::getId)
                .max(Integer::compareTo).orElse(0);
    }

    public static void addSshServer(SshServer sshServer) {
        ensureConfigLoadInMemory();
        CONFIG_PERSISTENCE.getSshServers().add(sshServer);
        SSH_SERVER_MAP.put(sshServer.getId(), sshServer);
    }

    public static void removeSshServer(SshServer sshServer) {
        ensureConfigLoadInMemory();
        CONFIG_PERSISTENCE.getSshServers().remove(sshServer);
        SSH_SERVER_MAP.remove(sshServer.getId());
    }

    public static void removeSshServer(Integer id) {
        ensureConfigLoadInMemory();
        SshServer sshServer = SSH_SERVER_MAP.get(id);
        if (sshServer == null) {
            return;
        }
        removeSshServer(sshServer);
        // delete command
        List<Command> commands = getCommandsBySshId(id);
        COMMAND_MAP.remove(id);
        commands.forEach(cmd -> CONFIG_PERSISTENCE.getCommands().remove(cmd));
        COMMAND_LIST =  CONFIG_PERSISTENCE.getCommands();
        // delete upload profile
        List<UploadProfile> profiles = getUploadProfileBySshId(id);
        UPLOAD_PROFILE_MAP.remove(id);
        profiles.forEach(profile -> CONFIG_PERSISTENCE.getUploadProfiles().remove(profile));
    }


    public static List<Command> getCommandsBySshId(int sshId) {
        ensureConfigLoadInMemory();
        return COMMAND_MAP.getOrDefault(sshId, new ArrayList<>());
    }

    public static List<Command> getSharableCommands(Integer excludeSshId) {
        ensureConfigLoadInMemory();
        return CONFIG_PERSISTENCE.getCommands().stream()
                .filter(Command::getSharable)
                .filter(cmd -> !Objects.equals(cmd.getSshId(), excludeSshId))
                .toList();
    }

    public static void addCommand(Command command) {
        ensureConfigLoadInMemory();
        CONFIG_PERSISTENCE.getCommands().add(command);
        COMMAND_LIST =  CONFIG_PERSISTENCE.getCommands();
        COMMAND_MAP = COMMAND_LIST.stream()
                .filter(it -> Objects.nonNull(it.getSshId()))
                .collect(Collectors.groupingBy(Command::getSshId));
    }

    public static void removeCommand(Command command) {
        ensureConfigLoadInMemory();
        COMMAND_LIST =  CONFIG_PERSISTENCE.getCommands();
        COMMAND_MAP = COMMAND_LIST.stream()
                .filter(it -> Objects.nonNull(it.getSshId()))
                .collect(Collectors.groupingBy(Command::getSshId));
    }

    public static Integer maxCommandId() {
        ensureConfigLoadInMemory();
        return CONFIG_PERSISTENCE.getCommands().stream()
                .map(Command::getId)
                .max(Integer::compareTo).orElse(0);
    }

    public static Command getCommandById(int id) {
        ensureConfigLoadInMemory();
        return CONFIG_PERSISTENCE.getCommands().stream()
                .filter(command -> command.getId() == id)
                .findFirst().orElse(null);
    }

    public static List<UploadProfile> getUploadProfileBySshId(int sshId) {
        ensureConfigLoadInMemory();
        return UPLOAD_PROFILE_MAP.getOrDefault(sshId, new ArrayList<>());
    }

    public static void addUploadProfile(UploadProfile uploadProfile) {
        ensureConfigLoadInMemory();
        CONFIG_PERSISTENCE.getUploadProfiles().add(uploadProfile);
        UPLOAD_PROFILE_MAP = CONFIG_PERSISTENCE.getUploadProfiles().stream()
                .collect(Collectors.groupingBy(UploadProfile::getSshId));
    }

    public static void removeUploadProfile(UploadProfile uploadProfile) {
        ensureConfigLoadInMemory();
        CONFIG_PERSISTENCE.getUploadProfiles().remove(uploadProfile);
        UPLOAD_PROFILE_MAP = CONFIG_PERSISTENCE.getUploadProfiles().stream()
                .collect(Collectors.groupingBy(UploadProfile::getSshId));
    }

    public static int maxUploadProfileId() {
        ensureConfigLoadInMemory();
        return CONFIG_PERSISTENCE.getUploadProfiles().stream()
                .filter(Objects::nonNull)
                .map(UploadProfile::getId)
                .max(Integer::compareTo).orElse(1);
    }

    public static boolean isUploadProfileExist(int profileId) {
        ensureConfigLoadInMemory();
        return CONFIG_PERSISTENCE.getUploadProfiles().stream()
                .anyMatch(profile -> Objects.equals(profile.getId(), profileId));
    }

    public static List<String> getServerTags() {
        ensureConfigLoadInMemory();
        return CONFIG_PERSISTENCE.getServerTags();
    }

    public static void setSshServerTags(List<String> newTags) {
        ensureConfigLoadInMemory();
        CONFIG_PERSISTENCE.setServerTags(newTags);
    }

    public static UploadProfile getOneUploadProfileById(int sshId, int profileId) {
        ensureConfigLoadInMemory();
        return getUploadProfileBySshId(sshId).stream()
                .filter(p -> Objects.equals(p.getId(), profileId))
                .findFirst().orElse(null);
    }

    public static PluginSetting pluginSetting() {
        ensureConfigLoadInMemory();
        return CONFIG_PERSISTENCE.getSetting();
    }
}