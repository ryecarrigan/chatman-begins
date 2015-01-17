package com.ryancarrigan.chatman;

/**
 * Created by Suave Peanut on 2015.1.11.
 */
public class Nick {

    private final String  nick;
    private final boolean online;

    public Nick(final String nick, final boolean online) {
        this.nick   = nick;
        this.online = online;
    }

    String getValues() {
        return String.format("('%s', %s)", this.nick, getOnline());
    }

    String getOnline() {
        return (online) ? "1" : "0";
    }

    @Override
    public String toString() {
        return getValues();
    }

}
