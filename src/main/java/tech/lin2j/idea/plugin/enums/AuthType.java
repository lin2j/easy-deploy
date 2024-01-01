package tech.lin2j.idea.plugin.enums;

/**
 * @author linjinjia
 * @date 2022/12/3 13:23
 */
public enum AuthType {

    /**
     * Using a password for authentication
     */
    PASSWORD(1, "password"),

    /**
     * Using a private key for authentication
     */
    PEM_PRIVATE_KEY(2, "private key");

    private final Integer code;
    private final String desc;

    AuthType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * @param code code of authenticate type
     * @return true if the code represents
     * authenticate with password
     */
    public static boolean needPassword(Integer code) {
        // the code parameter may be null
        // because server info stored in the
        // configuration has no authType value
        return code == null || PASSWORD.getCode().equals(code);
    }

    /**
     * @param code code of authenticate type
     * @return true if the code represents
     * authenticate with pem private key
     */
    public static boolean needPemPrivateKey(Integer code) {
        return PEM_PRIVATE_KEY.getCode().equals(code);
    }

    @Override
    public String toString() {
        return code.toString();
    }
}