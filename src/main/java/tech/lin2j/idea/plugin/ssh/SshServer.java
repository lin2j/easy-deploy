package tech.lin2j.idea.plugin.ssh;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.util.xmlb.annotations.Transient;
import tech.lin2j.idea.plugin.enums.AuthType;

/**
 * @author linjinjia
 * @date 2022/4/25 15:06
 */
public class SshServer implements Cloneable{

    private Integer id;

    private String ip;

    private Integer port;

    private String username;

    @Deprecated
    private String password;

    private String tag;

    private String description;

    /**
     * authenticate with password or other ways
     *
     * @see AuthType
     */
    private Integer authType;

    /**
     * file path of ssh private key，
     * the default value is "~/.ssh/id_rsa"
     */
    private String pemPrivateKey;

    private CredentialAttributes createCredentialAttributes(String key) {
        return new CredentialAttributes(
                CredentialAttributesKt.generateServiceName("Easy Deploy", key)
        );
    }

    private String getKey() {
        return "SD$" + username + "@" + ip + ":" + port;
    }

    @Transient
    public String getPassword() {
        CredentialAttributes attributes = createCredentialAttributes(getKey());
        PasswordSafe passwordSafe = PasswordSafe.getInstance();

        Credentials credentials = passwordSafe.get(attributes);
        if (credentials != null) {
            return credentials.getPasswordAsString();
        }
        return passwordSafe.getPassword(attributes);
    }

    @Transient
    public void setPassword(String password) {
        String key = getKey();
        CredentialAttributes attributes = createCredentialAttributes(key);
        Credentials credentials = new Credentials(key, password);
        PasswordSafe.getInstance().set(attributes, credentials);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        // forget history password in plugin configuration file
        this.password = "";
    }


    @Override
    public String toString() {
        return ip;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getAuthType() {
        return authType;
    }

    public void setAuthType(Integer authType) {
        this.authType = authType;
    }

    public String getPemPrivateKey() {
        return pemPrivateKey;
    }

    public void setPemPrivateKey(String pemPrivateKey) {
        this.pemPrivateKey = pemPrivateKey;
    }

    @Override
    public SshServer clone() {
        try {
            SshServer clone = (SshServer) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}