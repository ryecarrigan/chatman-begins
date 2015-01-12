package com.ryancarrigan.chatman;

/**
 * Created by Suave Peanut on 2015.1.1.
 */
public class Action {

    private final String  action;
    private final String  eventName;
    private final String  nick;

    public Action(final String data, final String eventName, final String nick) {
        this.action    = data;
        this.eventName = eventName;
        this.nick      = nick;
    }

    public Action(final Event event) {
        this(event.getRawDate(), event.getEventName(), event.getNick());
    }

    public String getAction() {
        return this.action;
    }

    public String getEventName() {
        return eventName;
    }

    public String getNick() {
        return this.nick;
    }

}
