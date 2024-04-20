package tech.lin2j.idea.plugin.enums;

/**
 * @author linjinjia
 * @date 2024/4/16 22:23
 */
public enum TransferCellState {

    UPLOADED("Uploaded", null),
    DOWNLOADED("Downloaded", null),
    UPLOADING("Uploading", UPLOADED),
    DOWNLOADING("Downloading", DOWNLOADED),
    ;

    private final String desc;
    private final TransferCellState nextState;

    TransferCellState(String desc, TransferCellState nextState) {
        this.desc = desc;
        this.nextState = nextState;
    }

    public TransferCellState nextState() {
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