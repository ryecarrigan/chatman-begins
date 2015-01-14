package com.ryancarrigan.chatman;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Suave Peanut on 2015.1.1.
 */
public class Reaction {

    private final static Logger logger = LoggerFactory.getLogger(Reaction.class);

    private final String   reactionType;
    private final String[] nicks;
    private final String   trigger;
    private final String   reaction;
    private final Integer  count;

    public Reaction(final String reactionType, final String nicks, final String trigger, final String reaction,
                    final Integer count) {
        this.reactionType = reactionType;
        this.nicks        = (nicks == null) ? new String[]{"NULL"} : nicks.split("\\|\\|");
        this.trigger      = trigger;
        this.reaction     = reaction;
        this.count        = count;
    }

    public Integer getCount() {
        return (count == null) ? 0 : count;
    }

    public String getReaction() {
        return this.reaction;
    }

    public String getReactionType() {
        return this.reactionType;
    }

    public boolean isTriggeredBy(final Action command) {
        return hasMatchingMessage(command) && hasMatchingNick(command);
    }

    private boolean hasMatchingMessage(final Action command) {
        return Pattern.compile(this.trigger).matcher(command.getAction()).find();
    }

    private boolean hasMatchingNick(final Action command) {
        final List<String> nicks = Arrays.asList(this.nicks);
        return nicks.contains(command.getNick()) || nicks.contains("NULL");
    }

}
