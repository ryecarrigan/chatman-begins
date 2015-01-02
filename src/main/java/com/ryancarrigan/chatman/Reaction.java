package com.ryancarrigan.chatman;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Suave Peanut on 2015.1.1.
 */
class Reaction {

    private final static Logger log = LoggerFactory.getLogger(Event.class);

    private final String channel;
    private final String data;
    private final String eventName;
    private final String nick;
    private final String target;

    public Reaction(final String channel, final String data, final String eventName, final String nick,
                    final String target) {
        this.channel   = channel;
        this.data      = data;
        this.eventName = eventName;
        this.nick      = nick;
        this.target    = target;
    }

    String getStatement() {
        return String.format("SELECT * FROM Reactions WHERE EventName=%s AND Channel=%s AND Nick=%s",
                getEventName(), getChannel(), getNick());
    }

    private String get(final Object parameter) {
        return (null == parameter) ? "NULL" : "'" + parameter.toString().replace("'", "\\'") + "'";
    }

    public String getChannel() {
        return get(this.channel);
    }

    public String getData() {
        return get(this.data);
    }

    public String getEventName() {
        return get(this.eventName);
    }

    public String getNick() {
        return get(this.nick);
    }

    public String getTarget() {
        return get(this.target);
    }

}
