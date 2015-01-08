package com.ryancarrigan.chatman;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Suave Peanut on 2015.1.1.
 */
public class Reaction {

    private final Integer count;
    private final String  action;
    private final String  channel;
    private final String  eventName;
    private final String  nick;
    private final String  reaction;
    private final String  target;

    public Reaction(final String channel, final String data, final String eventName, final String nick,
                    final String target, final String reaction, final Integer count) {
        this.channel   = channel;
        this.count     = count;
        this.action    = data;
        this.eventName = eventName;
        this.nick      = nick;
        this.reaction  = reaction;
        this.target    = target;
    }

    public String getStatement() {
        return String.format("SELECT * FROM Reactions WHERE EventName='%s' AND Channel='%s'",getEventName(), getChannel());
    }

    private String get(final Object parameter) {
        return (null == parameter) ? "NULL" : parameter.toString();
    }

    public String getAction() {
        return get(this.action);
    }

    public String getChannel() {
        return get(this.channel);
    }

    public Integer getCount() {
        return (count == null) ? 0 : count;
    }

    public String getEventName() {
        return get(this.eventName);
    }

    public String getNick() {
        return get(this.nick);
    }

    private List<String> getNicks() {
        return Arrays.asList(getNick().split("\\|\\|"));
    }

    public String getReaction() {
        return this.reaction;
    }

    public String getTarget() {
        return get(this.target);
    }

    public boolean hasMatchingMessage(final Action command) {
        return Pattern.compile(this.getAction()).matcher(command.getAction()).find();
    }

    public boolean hasMatchingNick(final Action command) {
        final List<String> nicks = getNicks();
        return nicks.contains(command.getNick()) || nicks.contains("NULL");
    }

}
