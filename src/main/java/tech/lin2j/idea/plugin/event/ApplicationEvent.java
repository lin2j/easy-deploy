package tech.lin2j.idea.plugin.event;


import java.util.EventObject;

/**
 * @author linjinjia
 * @date 2022/4/27 10:21
 */
public class ApplicationEvent extends EventObject {

    public ApplicationEvent(Object source) {
        super(source);
    }
}