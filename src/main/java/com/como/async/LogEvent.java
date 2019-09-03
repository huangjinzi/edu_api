package com.como.async;

import net.dreamlu.event.core.ApplicationEvent;

public class LogEvent extends ApplicationEvent {

    private static final long serialVersionUID = -8820786197616110800L;

    public LogEvent(Object source) {
        super(source);
    }

}
