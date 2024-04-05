package tech.lin2j.idea.plugin.domain.model.event;

import tech.lin2j.idea.plugin.event.ApplicationEvent;

/**
 * @author linjinjia
 * @date 2024/4/4 12:23
 */
public class FileTransferEvent extends ApplicationEvent {
    public FileTransferEvent(Object source) {
        super(source);
    }

    public FileTransferEvent() {
        super(new Object());
    }
}