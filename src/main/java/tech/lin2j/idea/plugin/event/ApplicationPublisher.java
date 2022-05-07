package tech.lin2j.idea.plugin.event;

/**
 * @author linjinjia
 * @date 2022/4/27 10:24
 */
@FunctionalInterface
public interface ApplicationPublisher {

    default void publishEvent(ApplicationEvent event) {
        publishEvent((Object) event);
    }

    void publishEvent(Object event);
}