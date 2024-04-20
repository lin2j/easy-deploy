package tech.lin2j.idea.plugin.domain.model.event;

import tech.lin2j.idea.plugin.enums.TransferEventState;
import tech.lin2j.idea.plugin.event.ApplicationEvent;

/**
 * @author linjinjia
 * @date 2024/4/4 12:23
 */
public class FileTransferEvent extends ApplicationEvent {

    private final TransferEventState state;

    private final boolean isUpload;

    public FileTransferEvent(Object source, boolean isUpload, TransferEventState state) {
        super(source);
        this.isUpload = isUpload;
        this.state = state;
    }

    public FileTransferEvent(boolean isUpload, TransferEventState state) {
        super(new Object());
        this.isUpload = isUpload;
        this.state = state;
    }

    public TransferEventState getState() {
        return state;
    }

    public boolean isUpload() {
        return isUpload;
    }
}