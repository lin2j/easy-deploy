package tech.lin2j.idea.plugin.enums;

/**
 * @author linjinjia
 * @date 2024/4/16 22:23
 */
public enum TransferState {

    UPLOADED("Uploaded", null),
    DOWNLOADED("Downloaded", null),
    UPLOADING("Uploading", UPLOADED),
    DOWNLOADING("Downloading", DOWNLOADED),

    FAILED("Failed", null),
    ;

    private final String desc;
    private final TransferState nextState;

    TransferState(String desc, TransferState nextState) {
        this.desc = desc;
        this.nextState = nextState;
    }

    public TransferState nextState() {
        if (nextState == null) {
            return this;
        }
        return nextState;
    }

    @Override
    public String toString() {
        return desc;
    }
}