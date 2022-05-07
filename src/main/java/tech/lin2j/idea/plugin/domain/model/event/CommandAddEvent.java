package tech.lin2j.idea.plugin.domain.model.event;

import tech.lin2j.idea.plugin.event.ApplicationEvent;

/**
 * @author linjinjia
 * @date 2022/4/29 10:06
 */
public class CommandAddEvent extends ApplicationEvent {
    public CommandAddEvent(Object source) {
        super(source);
    }

    public CommandAddEvent() {
        super(new Object());
    }
}