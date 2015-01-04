package com.ryancarrigan.chatman;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * Created by Suave Peanut on 2015.1.3.
 */
public class Peabot extends ChatBot {

    private final static Logger log = LoggerFactory.getLogger(Peabot.class);

    public Peabot(final String channel, final String login) {
        super(channel, login, "chatman_db", "chatman", "ch4tp455");
    }

    @Override
    void receiveEvent(final String eventName, final String channel, final String login, final String hostName,
                      final String nick, final String target, final String data, final Number number) {
        final Event event = new Event(eventName, channel, login, hostName, nick, target, data, number);
        logEvent(event);
        insertEvent(event);
        react(event);
    }

    private void sendMessage(final String message) {
        super.sendMessage(channel, message);
        final Event event = new Event("Message", channel, login, null, login, null, message, null);
        logEvent(event);
        insertEvent(event);
    }

    private void logEvent(final Event event) {
        log.info(event.getLogMessage());
    }

    private void insertEvent(final Event event) {
        getDataConnector().insert(event);
    }

    private void react(final Event event) {
        final Action command = new Action(channel, event.getData(), event.getEventName(), event.getNick(), event.getTarget());
        final List<Reaction> reactions = getDataConnector().react(command);
        final int size = reactions.size();
        if (size > 0) {
            for (final Reaction reaction : reactions) {
                final int count = reaction.getCount();
                for (int i = 0; i < count; ++i) {
                    if (reaction.hasMatchingMessage(command) && reaction.hasMatchingNick(command)) {
                        switch (reaction.getEventName()) {
                            case "Message":
                                sendMessage(reaction.getReaction());
                                break;
                            default:
                                break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (final InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

}
