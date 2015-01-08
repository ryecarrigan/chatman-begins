package com.ryancarrigan.chatman;

/**
 * Created by Suave Peanut on 2014.12.27.
 */
public class Event {

    private final String channel;
    private final String data;
    private final String eventName;
    private final String hostname;
    private final String login;
    private final String nick;
    private final Number number;
    private final String target;

    public Event(final IrkEvent event, final String channel, final String login, final String hostname,
          final String nick, final String target, final String data, final Number number) {
        this.eventName = event.getName();
        this.channel   = channel;
        this.login     = login;
        this.hostname  = hostname;
        this.nick      = nick;
        this.target    = target;
        this.data      = data;
        this.number    = number;
    }

    public String getStatement(final String table) {
        return String.format("INSERT INTO `%s` (EventName, Channel, Login, HostName, Nick, Target, Data, Number) "
                        + "values ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')", table, getEventName(),
                getChannel(), getLogin(), getHostname(), getNick(), getTarget(), getData(), getNumber())
                .replaceAll("\'NULL\'", "NULL");
    }

    private String get(final Object parameter) {
        return (null == parameter) ? "NULL" : parameter.toString();
    }

    public String getChannel() {
        return get(this.channel);
    }

    public String getData() {
        return get(this.data).replace("'", "\\'");
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
