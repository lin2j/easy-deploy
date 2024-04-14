package tech.lin2j.idea.plugin.enums;

/**
 * @author linjinjia
 * @date 2024/4/13 16:25
 */
public enum FileTransferState {

    START,
    END

    ;

    public boolean isEnd() {
        return this == END;
    }

}