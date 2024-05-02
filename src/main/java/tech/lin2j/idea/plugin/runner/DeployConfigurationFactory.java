package tech.lin2j.idea.plugin.runner;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author linjinjia
 * @date 2024/4/24 21:57
 */
public class DeployConfigurationFactory extends ConfigurationFactory {

    protected DeployConfigurationFactory(ConfigurationType type) {
        super(type);
    }

    @Override
    public @NotNull String getId() {
        return DeployRunConfigurationType.ID;
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(
            @NotNull Project project) {
        return new DeployRunConfiguration(project, this, "Easy Deploy");
    }

    @Nullable
    @Override
    public Class<? extends BaseState> getOptionsClass() {
        return DeployRunConfigurationOptions.class;
    }

}
