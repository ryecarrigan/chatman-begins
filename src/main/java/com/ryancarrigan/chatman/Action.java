package com.ryancarrigan.chatman;

/**
 * Created by Suave Peanut on 2015.1.1.
 */
public class Action {

    private final String  action;
    private final String  channel;
    private final String  eventName;
    private final String  nick;
    private final String  target;

    public Action(final String channel, final String data, final String eventName, final String nick,
                  final String target) {
        this.channel   = channel;
        this.action    = data;
        this.eventName = eventName;
        this.nick      = nick;
        this.target    = target;
    }

    public Action(final String channel, final Event event) {
        this(channel, event.getData(), event.getEventName(), event.getNick(), event.getTarget());
    }

    public String getStatement() {
        return String.format("SELECT * FROM Reactions WHERE EventName='%s' AND Channel='%s'",
                this.eventName, this.channel);
    }

    private String get(final Object parameter) {
        return (null == parameter) ? "NULL" : parameter.toString();
    }

    public String getAction() {
        return get(this.action);
    }

    public String getNick() {
        return get(this.nick);
    }

    public String getTarget() {
        return get(this.target);
    }

}
