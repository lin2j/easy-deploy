package tech.lin2j.idea.plugin.domain.model.event;

import tech.lin2j.idea.plugin.event.ApplicationEvent;

/**
 * @author linjinjia
 * @date 2022/4/27 10:28
 */
public class TableRefreshEvent extends ApplicationEvent {

    public TableRefreshEvent(Object source) {
        super(source);
    }

    public TableRefreshEvent() {
        super(new Object());
    }

}