package com.ryancarrigan.chatman;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Created by Suave Peanut on 2015.1.1.
 */
class Reaction {

    private final static Logger log = LoggerFactory.getLogger(Event.class);

    private final String action;
    private final String channel;
    private final String eventName;
    private final String nick;
    private final String reaction;
    private final String target;

    public Reaction(final String channel, final String data, final String eventName, final String nick,
                    final String target, final String reaction) {
        this.channel   = channel;
        this.action    = data;
        this.eventName = eventName;
        this.nick      = nick;
        this.reaction  = reaction;
        this.target    = target;
    }

    String getStatement() {
        return String.format("SELECT * FROM Reactions WHERE EventName='%s' AND Channel='%s'",getEventName(), getChannel());
    }

    private String get(final Object parameter) {
        return (null == parameter) ? "NULL" : parameter.toString().replace("'", "\\'");
    }

    public String getAction() {
        return get(this.action);
    }

    public String getChannel() {
        return get(this.channel);
    }

    public String getEventName() {
        return get(this.eventName);
    }

    public String getNick() {
        return get(this.nick);
    }

    public String getReaction() {
        return reaction;
    }

    public String getTarget() {
        return get(this.target);
    }

    public boolean hasMatchingMessage(final Reaction command) {
        log.info(String.format("Determining whether C:%s matches R:%s", command.getAction(), this.getAction()));
        return Pattern.compile(this.getAction()).matcher(command.getAction()).find();
    }

    public boolean hasMatchingNick(final Reaction command) {
        log.info(String.format("Determining whether C:%s matches R:%s", command.getNick(), this.getNick()));
        return this.getNick().equalsIgnoreCase(command.getNick()) || this.getNick().equalsIgnoreCase("NULL");
    }

    public String toString() {
        return String.format("Do %s: In channel %s when %s  says \"%s\" the %s responds with \"%s\"",
                getEventName(), getChannel(), getNick(), getAction(), getTarget(), getReaction());
    }

}
