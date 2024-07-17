package tech.lin2j.idea.plugin.model.event;

import tech.lin2j.idea.plugin.enums.TransferEventType;
import tech.lin2j.idea.plugin.event.ApplicationEvent;

/**
 * @author linjinjia
 * @date 2024/4/4 12:23
 */
public class FileTransferEvent extends ApplicationEvent {

    private final TransferEventType state;

    private final boolean isUpload;

    public FileTransferEvent(Object source, boolean isUpload, TransferEventType state) {
        super(source);
        this.isUpload = isUpload;
        this.state = state;
    }

    public FileTransferEvent(boolean isUpload, TransferEventType state) {
        super(new Object());
        this.isUpload = isUpload;
        this.state = state;
    }

    public TransferEventType getState() {
        return state;
    }

    public boolean isUpload() {
        return isUpload;
    }
}