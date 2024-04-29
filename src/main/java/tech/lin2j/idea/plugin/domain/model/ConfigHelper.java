package tech.lin2j.idea.plugin.domain.model;

import com.intellij.openapi.application.ApplicationManager;
import tech.lin2j.idea.plugin.ssh.SshServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author linjinjia
 * @date 2022/4/25 17:27
 */
public class ConfigHelper {

    private static final ConfigPersistence CONFIG_PERSISTENCE =
            ApplicationManager.getApplication().getService(ConfigPersistence.class);

    private static final Map<Long, SshServer> SSH_SERVER_MAP;

    private static Map<Long, List<Command>> COMMAND_MAP;

    private static Map<Long, List<UploadProfile>> UPLOAD_PROFILE_MAP;

    static {
        SSH_SERVER_MAP = CONFIG_PERSISTENCE.getSshServers().stream()
                .collect(Collectors.toMap(SshServer::getId, s -> s, (s1, s2) -> s1));

        COMMAND_MAP = CONFIG_PERSISTENCE.getCommands().stream()
                .collect(Collectors.groupingBy(Command::getSshId));

        UPLOAD_PROFILE_MAP = CONFIG_PERSISTENCE.getUploadProfiles().stream()
                .collect(Collectors.groupingBy(UploadProfile::getSshId));
    }

    public static SshServer getSshServerById(int id) {
        return SSH_SERVER_MAP.get(id);
    }

    public static List<SshServer> sshServers() {
        return CONFIG_PERSISTENCE.getSshServers();
    }

    public static long maxSshServerId() {
        return CONFIG_PERSISTENCE.getSshServers().stream()
                .map(SshServer::getId)
                .max(Long::compareTo).orElse(0L);
    }

    public static void addSshServer(SshServer sshServer) {
        CONFIG_PERSISTENCE.getSshServers().add(sshServer);
        SSH_SERVER_MAP.put(sshServer.getId(), sshServer);
    }

    public static void removeSshServer(SshServer sshServer) {
        CONFIG_PERSISTENCE.getSshServers().remove(sshServer);
        SSH_SERVER_MAP.remove(sshServer.getId());
    }

    public static void removeSshServer(long id) {
        SshServer sshServer = SSH_SERVER_MAP.get(id);
        if (sshServer == null) {
            return;
        }
        removeSshServer(sshServer);
        // delete command
        List<Command> commands = getCommandsBySshId(id);
        COMMAND_MAP.remove(id);
        commands.forEach(cmd -> CONFIG_PERSISTENCE.getCommands().remove(cmd));
        // delete upload profile
        List<UploadProfile> profiles = getUploadProfileBySshId(id);
        UPLOAD_PROFILE_MAP.remove(id);
        profiles.forEach(profile -> CONFIG_PERSISTENCE.getUploadProfiles().remove(profile));
    }

    public static List<Command> getCommandsBySshId(long sshId) {
        return COMMAND_MAP.getOrDefault(sshId, new ArrayList<>());
    }

    public static void addCommand(Command command) {
        CONFIG_PERSISTENCE.getCommands().add(command);
        COMMAND_MAP = CONFIG_PERSISTENCE.getCommands().stream()
                .collect(Collectors.groupingBy(Command::getSshId));
    }

    public static void removeCommand(Command command) {
        CONFIG_PERSISTENCE.getCommands().remove(command);
        COMMAND_MAP = CONFIG_PERSISTENCE.getCommands().stream()
                .collect(Collectors.groupingBy(Command::getSshId));
    }

    public static Long maxCommandId() {
        return CONFIG_PERSISTENCE.getCommands().stream()
                .map(Command::getId)
                .max(Long::compareTo).orElse(0L);
    }

    public static Command getCommandById(long id) {
        return CONFIG_PERSISTENCE.getCommands().stream()
                .filter(command -> command.getId() == id)
                .findFirst().orElse(null);
    }

    public static List<UploadProfile> getUploadProfileBySshId(long sshId) {
        return UPLOAD_PROFILE_MAP.getOrDefault(sshId, new ArrayList<>());
    }

    public static void addUploadProfile(UploadProfile uploadProfile) {
        CONFIG_PERSISTENCE.getUploadProfiles().add(uploadProfile);
        UPLOAD_PROFILE_MAP = CONFIG_PERSISTENCE.getUploadProfiles().stream()
                .collect(Collectors.groupingBy(UploadProfile::getSshId));
    }

    public static void removeUploadProfile(UploadProfile uploadProfile) {
        CONFIG_PERSISTENCE.getUploadProfiles().remove(uploadProfile);
        UPLOAD_PROFILE_MAP = CONFIG_PERSISTENCE.getUploadProfiles().stream()
                .collect(Collectors.groupingBy(UploadProfile::getSshId));
    }

    public static List<String> getServerTags() {
        return CONFIG_PERSISTENCE.getServerTags();
    }
}