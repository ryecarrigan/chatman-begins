package com.ryancarrigan.peabot;

import com.ryancarrigan.chatman.*;
import org.jibble.pircbot.IrcException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Suave Peanut on 2015.1.7.
 */
public class Peabot extends Irksome {

    private DataConnector            dataConnector;
    private Queue<String>            messageQueue = new LinkedList<>();
    private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    public Peabot(final String channel, final String login, final String databaseName,
                  final String databaseLogin, final String databasePassword) {
        super(channel, login);
        this.dataConnector = new DataConnector(databaseName, databaseLogin, databasePassword, channel);
    }

    @Override
    public void quit() {
        try {
            this.dataConnector.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receiveEvent(final IrkEvent event, final String channel, final String login, final String hostName,
                             final String nick, final String target, final String data, final Number number) {
        final Event irk = new Event(event, channel, login, hostName, nick, target, data, number);
        react(irk);
    }

    @Override
    public void start() {
        try {
            this.dataConnector.connect();
            final String password = this.dataConnector.getPassword(this.getName());
            this.connect("irc.twitch.tv", 6667, password);
        } catch (SQLException | IrcException | IOException e) {
            e.printStackTrace();
        }
        this.joinChannel(this.getChannel());
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                final String message = messageQueue.poll();
                if (message != null) {
                    sendMessage(getChannel(), message);
//                    System.out.println(String.format("%s: %s", getNick(), message));
                }
            }
        }, 0, 675, TimeUnit.MILLISECONDS);
    }

    private void react(final Event event) {
        final Action command = new Action(this.getChannel(), event);
        final List<Reaction> reactions = this.dataConnector.react(command);
        if(reactions.size() > 0) {
            for (final Reaction reaction : reactions) {
                if (reaction.hasMatchingMessage(command) && reaction.hasMatchingNick(command)) {
//                    System.out.println(String.format("%s: %s", event.getNick(), event.getData()));
                    addMessage(reaction);
                }
            }
        }
    }

    void addMessage(final Reaction reaction) {
        final int    count        = reaction.getCount();
        final String reactionName = reaction.getEventName();
        for (int i = 0; i < count; ++i) {
            switch (reactionName) {
                case "Message":
                    Collections.addAll(messageQueue, reaction.getReaction().split("\\|\\|"));
                default:
            }
        }
    }

}
