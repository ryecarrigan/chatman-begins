package com.ryancarrigan.chatman;

/**
 * Created by Suave Peanut on 2015.1.1.
 */
public class Action {

    private final String  action;
    private final int eventKey;
    private final String  nick;

    public Action(final String data, final int eventKey, final String nick) {
        this.action   = data;
        this.eventKey = eventKey;
        this.nick     = nick;
    }

    public Action(final Event event) {
        this(event.getRawDate(), event.getEventKey(), event.getNick());
    }

    public String getAction() {
        return this.action;
    }

    public int getEventKey() {
        return eventKey;
    }

    public String getNick() {
        return this.nick;
    }

}
