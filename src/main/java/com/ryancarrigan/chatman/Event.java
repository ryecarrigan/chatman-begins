package com.ryancarrigan.chatman;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Suave Peanut on 2014.12.27.
 */
public class Event {

    private final String data;
    private final int    eventKey;
    private final String nick;

    public Event(final EventName event, final String nick, final String data) {
        this.eventKey  = event.getKey();
        this.nick      = nick;
        this.data      = data;
    }

    public String getValues() {
        return String.format("(%s, '%s', '%s', '%s')", getEventKey(), getNick(), getData(), getEventTime())
                .replaceAll("\'NULL\'", "NULL");
    }

    private String get(final Object parameter) {
        return (null == parameter) ? "NULL" : parameter.toString();
    }

    public String getData() {
        return get(this.data)
                .replace("\\", "\\\\")
                .replace("'", "\\'");
    }

    private String getEventTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public int getEventKey() {
        return this.eventKey;
    }

    public String getNick() {
        return get(this.nick);
    }

    public String getRawDate() {
        return this.data;
    }

    public String toString() {
        return this.getValues();
    }

}
