package com.ryancarrigan.chatman;

/**
 * Created by Suave Peanut on 2015.1.11.
 */
public class Nick {

    private final String nick;
    private final String online;

    public Nick(final String nick) {
        this.nick   = nick;
        this.online = "1";
    }

    String getValues() {
        return String.format("('%s', %s)", this.nick, this.online);
    }

    @Override
    public String toString() {
        return getValues();
    }

}
