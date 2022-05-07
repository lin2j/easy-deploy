package tech.lin2j.idea.plugin.event;

import com.intellij.openapi.diagnostic.Logger;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author linjinjia
 * @date 2022/4/27 10:30
 */
public class ApplicationContext implements ApplicationPublisher {

    private static final Logger log = Logger.getInstance(ApplicationContext.class);

    private static final ApplicationContext APPLICATION_CONTEXT = new ApplicationContext();

    private final Set<ApplicationListener<?>> listeners = new CopyOnWriteArraySet<>();

    public void addApplicationListener(ApplicationListener<?> listener) {
        listeners.add(listener);
    }

    public void removeApplicationListener(ApplicationListener<?> listener) {
        listeners.remove(listener);
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void publishEvent(Object event) {
        ApplicationEvent applicationEvent;
        if (event instanceof ApplicationEvent) {
            applicationEvent = (ApplicationEvent) event;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("event is not ApplicationEvent");
            }
            return;
        }
        for (final ApplicationListener listener : listeners) {
            try {
                listener.onApplicationEvent(applicationEvent);
            } catch (ClassCastException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Non-matching event type for listener: " + listener, e);
                }
            }
        }
    }

    public static ApplicationContext getApplicationContext() {
        return APPLICATION_CONTEXT;
    }
}