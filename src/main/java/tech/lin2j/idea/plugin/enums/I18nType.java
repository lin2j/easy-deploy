package tech.lin2j.idea.plugin.enums;

/**
 * @author linjinjia
 * @date 2024/6/29 21:25
 */
public enum I18nType {

    English(0, "English"),
    Chinese(1, "简体中文"),
    ;

    private final Integer type;
    private final String text;

    I18nType(Integer type, String text) {
        this.type = type;
        this.text = text;
    }

    public static I18nType getByType(int type) {
        for (I18nType value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return English;
    }

    public Integer getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }
}