package tech.lin2j.idea.plugin.model;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import tech.lin2j.idea.plugin.enums.TransferMode;
import tech.lin2j.idea.plugin.ssh.SshServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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

    private static List<UploadProfile> UPLOAD_PROFILE_LIST;

    private static List<io.github.yueryou.easydev.plugin.model.Pipeline> PIPELINE_LIST;

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

        // Migrate old configs with sshId to new global configs
        migrateOldConfigsWithSshId();

        COMMAND_LIST = CONFIG_PERSISTENCE.getCommands();

        UPLOAD_PROFILE_LIST = CONFIG_PERSISTENCE.getUploadProfiles();

        PIPELINE_LIST = CONFIG_PERSISTENCE.getPipelines();
    }

    /**
     * Migrate old configuration where Command and UploadProfile were bound to sshId.
     * Old format: Command and UploadProfile had sshId field, IDs were only unique per server.
     * New format: Command and UploadProfile are global, IDs are globally unique.
     */
    private static void migrateOldConfigsWithSshId() {
        List<Command> commands = CONFIG_PERSISTENCE.getCommands();
        List<UploadProfile> profiles = CONFIG_PERSISTENCE.getUploadProfiles();

        boolean hasOldCommand = commands.stream().anyMatch(cmd -> cmd.getSshId() != null);
        boolean hasOldProfile = profiles.stream().anyMatch(p -> p.getSshId() != null);

        if (!hasOldCommand && !hasOldProfile) {
            return; // No migration needed
        }

        log.info("Starting migration of old configs with sshId binding");

        // Migrate Commands first - create new global commands for each sshId+id combination
        Map<String, Integer> commandIdMigrationMap = new java.util.HashMap<>();
        if (hasOldCommand) {
            List<Command> oldCommands = commands.stream()
                    .filter(cmd -> cmd.getSshId() != null)
                    .toList();

            int maxCommandId = commands.stream()
                    .map(Command::getId)
                    .filter(Objects::nonNull)
                    .max(Integer::compareTo).orElse(0);

            for (Command oldCmd : oldCommands) {
                // Create new global command with unique ID
                Command newCmd = new Command();
                newCmd.setId(++maxCommandId);
                newCmd.setUid(UUID.randomUUID().toString());
                newCmd.setTitle(oldCmd.getTitle());
                newCmd.setDir(oldCmd.getDir());
                newCmd.setContent(oldCmd.getContent());
                newCmd.setSharable(oldCmd.getSharable());
                commands.add(newCmd);

                // Record the mapping: old sshId+id -> new global id
                String oldKey = oldCmd.getSshId() + "_" + oldCmd.getId();
                commandIdMigrationMap.put(oldKey, newCmd.getId());

                log.info("Migrated Command: sshId=" + oldCmd.getSshId() + ", oldId=" + oldCmd.getId() +
                        " -> newId=" + newCmd.getId() + ", title=" + newCmd.getTitle());
            }

            // Remove old commands with sshId
            commands.removeIf(cmd -> cmd.getSshId() != null);
        }

        // Migrate UploadProfiles - create new global profiles for each sshId+id combination
        if (hasOldProfile) {
            List<UploadProfile> oldProfiles = profiles.stream()
                    .filter(p -> p.getSshId() != null)
                    .toList();

            int maxProfileId = profiles.stream()
                    .map(UploadProfile::getId)
                    .filter(Objects::nonNull)
                    .max(Integer::compareTo).orElse(0);

            for (UploadProfile oldProfile : oldProfiles) {
                // Create new global profile with unique ID
                UploadProfile newProfile = new UploadProfile();
                newProfile.setId(++maxProfileId);
                newProfile.setUid(UUID.randomUUID().toString());
                newProfile.setName(oldProfile.getName());
                newProfile.setFile(oldProfile.getFile());
                newProfile.setLocation(oldProfile.getLocation());
                newProfile.setExclude(oldProfile.getExclude());
                newProfile.setIncludeCurrentDir(oldProfile.getIncludeCurrentDir());
                newProfile.setUseUploadPath(oldProfile.getUseUploadPath());

                // Migrate command IDs using the migration map
                if (oldProfile.getPreCommandId() != null) {
                    String oldKey = oldProfile.getSshId() + "_" + oldProfile.getPreCommandId();
                    Integer newCmdId = commandIdMigrationMap.get(oldKey);
                    if (newCmdId != null) {
                        newProfile.setPreCommandId(newCmdId);
                    }
                }
                if (oldProfile.getPostCommandId() != null) {
                    String oldKey = oldProfile.getSshId() + "_" + oldProfile.getPostCommandId();
                    Integer newCmdId = commandIdMigrationMap.get(oldKey);
                    if (newCmdId != null) {
                        newProfile.setPostCommandId(newCmdId);
                    }
                }
                // Handle backward compatibility for commandId field
                if (oldProfile.getCommandId() != null) {
                    String oldKey = oldProfile.getSshId() + "_" + oldProfile.getCommandId();
                    Integer newCmdId = commandIdMigrationMap.get(oldKey);
                    if (newCmdId != null) {
                        newProfile.setCommandId(newCmdId);
                    }
                }

                profiles.add(newProfile);

                log.info("Migrated UploadProfile: sshId=" + oldProfile.getSshId() + ", oldId=" + oldProfile.getId() +
                        " -> newId=" + newProfile.getId() + ", name=" + newProfile.getName());
            }

            // Remove old profiles with sshId
            profiles.removeIf(p -> p.getSshId() != null);
        }

        log.info("Migration of old configs completed");
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
        // Note: Commands and UploadProfiles are now global and not deleted when server is removed
    }

    public static List<Command> getAllCommands() {
        ensureConfigLoadInMemory();
        return COMMAND_LIST;
    }

    public static List<Command> getSharableCommands() {
        ensureConfigLoadInMemory();
        return COMMAND_LIST.stream()
                .filter(Command::getSharable)
                .toList();
    }

    public static void addCommand(Command command) {
        ensureConfigLoadInMemory();
        CONFIG_PERSISTENCE.getCommands().add(command);
        COMMAND_LIST = CONFIG_PERSISTENCE.getCommands();
    }

    public static void removeCommand(Command command) {
        ensureConfigLoadInMemory();
        CONFIG_PERSISTENCE.getCommands().remove(command);
        COMMAND_LIST = CONFIG_PERSISTENCE.getCommands();
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
                .filter(command -> Objects.equals(command.getId(), id))
                .findFirst().orElse(null);
    }

    public static List<UploadProfile> getAllUploadProfiles() {
        ensureConfigLoadInMemory();
        return UPLOAD_PROFILE_LIST;
    }

    public static void addUploadProfile(UploadProfile uploadProfile) {
        ensureConfigLoadInMemory();
        CONFIG_PERSISTENCE.getUploadProfiles().add(uploadProfile);
        UPLOAD_PROFILE_LIST = CONFIG_PERSISTENCE.getUploadProfiles();
    }

    public static void removeUploadProfile(UploadProfile uploadProfile) {
        ensureConfigLoadInMemory();
        CONFIG_PERSISTENCE.getUploadProfiles().remove(uploadProfile);
        UPLOAD_PROFILE_LIST = CONFIG_PERSISTENCE.getUploadProfiles();
    }

    public static int maxUploadProfileId() {
        ensureConfigLoadInMemory();
        return UPLOAD_PROFILE_LIST.stream()
                .filter(Objects::nonNull)
                .map(UploadProfile::getId)
                .max(Integer::compareTo).orElse(1);
    }

    public static boolean isUploadProfileExist(int profileId) {
        ensureConfigLoadInMemory();
        return UPLOAD_PROFILE_LIST.stream()
                .anyMatch(profile -> Objects.equals(profile.getId(), profileId));
    }

    public static UploadProfile getUploadProfileById(int profileId) {
        ensureConfigLoadInMemory();
        return UPLOAD_PROFILE_LIST.stream()
                .filter(p -> Objects.equals(p.getId(), profileId))
                .findFirst().orElse(null);
    }

    public static List<String> getServerTags() {
        ensureConfigLoadInMemory();
        return CONFIG_PERSISTENCE.getServerTags();
    }

    public static void setSshServerTags(List<String> newTags) {
        ensureConfigLoadInMemory();
        CONFIG_PERSISTENCE.setServerTags(newTags);
    }

    // Pipeline methods

    public static List<io.github.yueryou.easydev.plugin.model.Pipeline> getAllPipelines() {
        ensureConfigLoadInMemory();
        return PIPELINE_LIST;
    }

    public static io.github.yueryou.easydev.plugin.model.Pipeline getPipelineById(String id) {
        ensureConfigLoadInMemory();
        return PIPELINE_LIST.stream()
                .filter(p -> Objects.equals(p.getId(), id))
                .findFirst().orElse(null);
    }

    public static io.github.yueryou.easydev.plugin.model.Pipeline getPipelineByUid(String uid) {
        ensureConfigLoadInMemory();
        return PIPELINE_LIST.stream()
                .filter(p -> Objects.equals(p.getUid(), uid))
                .findFirst().orElse(null);
    }

    public static void addPipeline(io.github.yueryou.easydev.plugin.model.Pipeline pipeline) {
        ensureConfigLoadInMemory();
        CONFIG_PERSISTENCE.getPipelines().add(pipeline);
        PIPELINE_LIST = CONFIG_PERSISTENCE.getPipelines();
    }

    public static void removePipeline(io.github.yueryou.easydev.plugin.model.Pipeline pipeline) {
        ensureConfigLoadInMemory();
        CONFIG_PERSISTENCE.getPipelines().remove(pipeline);
        PIPELINE_LIST = CONFIG_PERSISTENCE.getPipelines();
    }

    public static PluginSetting pluginSetting() {
        ensureConfigLoadInMemory();
        return CONFIG_PERSISTENCE.getSetting();
    }
}
