package tech.lin2j.idea.plugin.runner;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.NotNullLazyValue;
import icons.MyIcons;

/**
 * @author linjinjia
 * @date 2024/4/24 21:57
 */
public class DeployRunConfigurationType extends ConfigurationTypeBase {
    static final String ID = "EasyDeployRunConfiguration";

    DeployRunConfigurationType() {
        super(ID, "Easy Deploy", "Deploy service to remote server",
                NotNullLazyValue.createValue(() -> MyIcons.EasyDeploy));
        addFactory(new DeployConfigurationFactory(this));
    }

}
