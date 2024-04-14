package tech.lin2j.idea.plugin.domain.model.event;

import tech.lin2j.idea.plugin.enums.FileTransferState;
import tech.lin2j.idea.plugin.event.ApplicationEvent;

/**
 * @author linjinjia
 * @date 2024/4/4 12:23
 */
public class FileTransferEvent extends ApplicationEvent {

    private final FileTransferState state;

    private final boolean isUpload;

    public FileTransferEvent(Object source, boolean isUpload, FileTransferState state) {
        super(source);
        this.isUpload = isUpload;
        this.state = state;
    }

    public FileTransferEvent(boolean isUpload, FileTransferState state) {
        super(new Object());
        this.isUpload = isUpload;
        this.state = state;
    }

    public FileTransferState getState() {
        return state;
    }

    public boolean isUpload() {
        return isUpload;
    }
}