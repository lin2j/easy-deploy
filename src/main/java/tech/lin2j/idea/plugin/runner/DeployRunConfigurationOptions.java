package tech.lin2j.idea.plugin.runner;

import com.intellij.execution.configurations.RunConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;

import java.util.Set;

/**
 * @author linjinjia
 * @date 2024/4/24 21:57
 */
public class DeployRunConfigurationOptions extends RunConfigurationOptions {
    private final StoredProperty<String> myScriptName =
            string("").provideDelegate(this, "scriptName");

    private final StoredProperty<Set<String>> deployProfile =
            this.stringSet().provideDelegate(this, "deployProfile");
    public String getScriptName() {
        return myScriptName.getValue(this);
    }

    public void setScriptName(String scriptName) {
        myScriptName.setValue(this, scriptName);
    }

    public Set<String> getDeployProfile() {
        return deployProfile.getValue(this);
    }

    /**
     * profile: sshId@uploadProfileName
     */
    public void addDeployProfile(String profile) {
        deployProfile.getValue(this).add(profile);
    }

    public void removeDeployProfile(String profile) {
        deployProfile.getValue(this).remove(profile);
    }
}
