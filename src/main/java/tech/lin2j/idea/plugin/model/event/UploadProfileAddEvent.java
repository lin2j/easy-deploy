package tech.lin2j.idea.plugin.model.event;

import tech.lin2j.idea.plugin.event.ApplicationEvent;

/**
 * @author linjinjia
 * @date 2022/4/29 10:06
 */
public class UploadProfileAddEvent extends ApplicationEvent {
    public UploadProfileAddEvent(Object source) {
        super(source);
    }

    public UploadProfileAddEvent() {
        super(new Object());
    }
}