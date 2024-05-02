package tech.lin2j.idea.plugin.runner;

import com.intellij.configurationStore.XmlSerializer;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.util.xmlb.annotations.Tag;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author linjinjia
 * @date 2024/4/24 21:57
 */
public class DeployRunConfiguration extends RunConfigurationBase<Element> {

    private List<String> deployProfiles;

    protected DeployRunConfiguration(Project project,
                                     ConfigurationFactory factory,
                                     String name) {
        super(project, factory, name);
    }


    @Override
    public void writeExternal(@NotNull Element element) {
        if (this.deployProfiles == null) {
            this.deployProfiles = new ArrayList<>();
        }
        ConfigurationState state = new ConfigurationState();
        state.deployProfiles = this.deployProfiles;
        XmlSerializer.serializeObjectInto(state, element);
        super.writeExternal(element);

    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        ConfigurationState state = XmlSerializer.deserialize(element, ConfigurationState.class);
        this.deployProfiles = state.deployProfiles;
        if (this.deployProfiles == null) {
            this.deployProfiles = new ArrayList<>();
        }
    }

    public void setDeployProfiles(List<String> deployProfiles) {
        this.deployProfiles = deployProfiles;
    }

    public List<String> getDeployProfile() {
        if (this.deployProfiles == null) {
            deployProfiles = new ArrayList<>();
        }
        return this.deployProfiles;
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new DeploySettingsEditor();
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor,
                                    @NotNull ExecutionEnvironment environment) {
        return new DeployRunProfileState(executor, environment, deployProfiles);
    }

    public static class ConfigurationState {
        @Tag("deploy-profiles")
        private List<String> deployProfiles;
    }
}
