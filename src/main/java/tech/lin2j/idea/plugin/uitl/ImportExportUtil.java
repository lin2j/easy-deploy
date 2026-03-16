package tech.lin2j.idea.plugin.uitl;

import com.google.gson.GsonBuilder;
import org.apache.commons.collections.CollectionUtils;
import tech.lin2j.idea.plugin.model.Command;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.ConfigImportExport;
import tech.lin2j.idea.plugin.model.ExportOptions;
import tech.lin2j.idea.plugin.model.UploadProfile;
import tech.lin2j.idea.plugin.ssh.SshServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author linjinjia
 * @date 2024/7/17 20:54
 */
public class ImportExportUtil {

    private ImportExportUtil() {
    }

    /**
     * Exports configuration based on the provided options.
     *
     * @param options The options specifying which fields to export.
     * @return A {@link ConfigImportExport} object containing the exported configuration data.
     */
    public static ConfigImportExport exportBaseOnOptions(ExportOptions options) throws Exception {
        ConfigImportExport dto = new ConfigImportExport();
        dto.setOptions(options);
        dto.setVersion(EasyDeployPluginUtil.version());
        if (options.isServerTags()) {
            dto.setServerTags(ConfigHelper.getServerTags());
        }

        List<SshServer> sshServers = ConfigHelper.sshServers();
        if (CollectionUtils.isEmpty(sshServers)) {
            return dto;
        }

        List<ConfigImportExport.HostInfo> hostInfos = new ArrayList<>();
        for (SshServer server : sshServers) {
            ConfigImportExport.HostInfo hostInfo = new ConfigImportExport.HostInfo();
            hostInfo.setServer(server.clone());

            // Commands and upload profiles are now global, not per-server
            // Export all commands and profiles
            if (options.isCommand()) {
                List<Command> cloneCommands = new ArrayList<>();
                for (Command command : ConfigHelper.getAllCommands()) {
                    cloneCommands.add(new Command(command));
                }
                hostInfo.setCommands(cloneCommands);
            }

            if (options.isUploadProfile()) {
                List<UploadProfile> cloneProfiles = new ArrayList<>();
                for (UploadProfile uploadProfile : ConfigHelper.getAllUploadProfiles()) {
                    cloneProfiles.add(uploadProfile.clone());
                }
                hostInfo.setUploadProfiles(cloneProfiles);
            }

            hostInfos.add(hostInfo);
        }
        dto.setHostInfos(hostInfos);

        return dto;
    }

    /**
     * Imports the provided configuration.
     *
     * @param newConfig The new configuration to be imported.
     * @return A {@link ConfigImportExport} object containing the old configuration data.
     * @throws Exception If an error occurs during the import process.
     */
    public static ConfigImportExport importConfig(ConfigImportExport newConfig) throws Exception {
        ConfigImportExport origin = exportBaseOnOptions(allExport());
        ExportOptions options = newConfig.getOptions();
        // server tag
        if (options.isServerTags()) {
            List<String> serverTags = ConfigHelper.getServerTags();
            List<String> newServerTags = newConfig.getServerTags();
            for (String nst : newServerTags) {
                if (!serverTags.contains(nst)) {
                    serverTags.add(nst);
                }
            }
        }

        // server
        Map<Integer, Integer> commandIdMap = new HashMap<>();
        Map<Integer, Integer> sshIdMap = new HashMap<>();
        for (ConfigImportExport.HostInfo hostInfo : newConfig.getHostInfos()) {
            SshServer newSever = hostInfo.getServer();
            int oldSshId = newSever.getId();
            int newSshId = ConfigHelper.maxSshServerId() + 1;
            newSever.setId(newSshId);
            ConfigHelper.addSshServer(newSever);
            sshIdMap.put(oldSshId, newSshId);
            // command
            // Commands are now global, import without sshId binding
            if (options.isCommand() && CollectionUtils.isNotEmpty(hostInfo.getCommands())) {
                hostInfo.getCommands().forEach(newCmd -> {
                    int oldCmdId = newCmd.getId();
                    int newCmdId = ConfigHelper.maxCommandId() + 1;
                    newCmd.setId(newCmdId);
                    newCmd.setSshId(null); // Clear sshId for new global format
                    ConfigHelper.addCommand(newCmd);

                    commandIdMap.put(oldCmdId, newCmdId);
                });
            }
            // upload profile
            // Upload profiles are now global, import without sshId binding
            if (options.isUploadProfile() && CollectionUtils.isNotEmpty(hostInfo.getUploadProfiles())) {
                hostInfo.getUploadProfiles().forEach(newProfile -> {
                    newProfile.setId(ConfigHelper.maxUploadProfileId() + 1);
                    newProfile.setSshId(null); // Clear sshId for new global format
                    if (newProfile.getCommandId() != null) {
                        newProfile.setCommandId(commandIdMap.get(newProfile.getCommandId()));
                    }
                    ConfigHelper.addUploadProfile(newProfile);
                });
            }
        }

        // proxy
        sshIdMap.forEach((oldId, newId) -> {
            SshServer newSshServer = ConfigHelper.getSshServerById(newId);
            if (newSshServer.getProxy() != null) {
                int oldProxyId = newSshServer.getProxy();
                newSshServer.setProxy(sshIdMap.get(oldProxyId));
            }
        });

        return origin;
    }

    public static ExportOptions allExport() {
        ExportOptions options = new ExportOptions();
        options.setServerTags(true);
        options.setCommand(true);
        options.setUploadProfile(true);
        return options;
    }

    public static void exportConfigToJsonFile(ConfigImportExport data,
                                              String filepath,
                                              String password) throws IOException {
        String content = new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(data);
        EncryptionUtil.encryptContentIntoFile(content, filepath, password);
    }
}