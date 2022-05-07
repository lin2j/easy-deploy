package tech.lin2j.idea.plugin.event;

import java.util.EventListener;

/**
 * @author linjinjia
 * @date 2022/4/27 10:22
 */
@FunctionalInterface
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {
    void onApplicationEvent(E event);
}