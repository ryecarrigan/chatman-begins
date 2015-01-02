package com.ryancarrigan.chatman;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Suave Peanut on 2014.12.27.
 */
class Event {

    private final static Logger log = LoggerFactory.getLogger(Event.class);

    private final String channel;
    private final String data;
    private final String eventName;
    private final String hostname;
    private final String login;
    private final String nick;
    private final Number number;
    private final String target;

    Event(final String eventName, final String channel, final String login, final String hostname,
          final String nick, final String target, final String data, final Number number) {
        this.eventName = eventName;
        this.channel   = channel;
        this.login     = login;
        this.hostname  = hostname;
        this.nick      = nick;
        this.target    = target;
        this.data      = data;
        this.number    = number;
    }

    String getStatement(final String table) {
        return String.format("INSERT INTO `%s` (EventName, Channel, Login, HostName, Nick, Target, Data, Number) "
                        + "values (%s, %s, %s, %s, %s, %s, %s, %s)", table, getEventName(), getChannel(), getLogin(),
                getHostname(), getNick(), getTarget(), getData(), getNumber());
    }

    void log() {
        log.info(String.format("<%s> (%s) - %s : %s : %s - %s { %s } [%s]", getEventName(), getChannel(), getLogin(),
                getHostname(), getNick(), getTarget(), getData(), getNumber()));
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

    public String getHostname() {
        return get(this.hostname);
    }

    public String getLogin() {
        return get(this.login);
    }

    public String getNick() {
        return get(this.nick);
    }

    public String getNumber() {
        return get(this.number);
    }

    public String getTarget() {
        return get(this.target);
    }

}
