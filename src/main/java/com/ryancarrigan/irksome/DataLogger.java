package com.ryancarrigan.irksome;

import com.ryancarrigan.chatman.Event;
import com.ryancarrigan.chatman.IrkEvent;
import com.ryancarrigan.chatman.Irksome;
import com.ryancarrigan.data.Datman;
import org.jibble.pircbot.IrcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Suave Peanut on 2015.1.6.
 */
public class DataLogger extends Irksome {

    private Logger      logger = LoggerFactory.getLogger(DataLogger.class);
    private List<Event> messageQueue = new LinkedList<>();

    public DataLogger(final String channel, final String login) {
        super(channel, login);
    }

    private void scheduleMessageService() {
        final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!messageQueue.isEmpty()) {
                    Datman.insertEvents(messageQueue);
                    messageQueue.clear();
                }
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    @Override
    public void quit() {
        Datman.setBotStatus(0);
        Datman.quit();
    }

    @Override
    public void receiveEvent(final IrkEvent event, final String login, final String hostName, final String nick, final String target, final String data, final Number number) {
        final Event irk = new Event(event, login, hostName, nick, target, data, number);
        messageQueue.add(irk);
    }

    @Override
    public void start() {
        scheduleMessageService();
        try {
            joinChannel();
        } catch (SQLException | IrcException | IOException e) {
            throw new RuntimeException(e);
        }
        Datman.setBotStatus(1);
    }

    private void joinChannel() throws SQLException, IrcException, IOException {
        final String channel  = getChannel();
        final String password = Datman.getIrcPassword(getName());
        connect("irc.twitch.tv", 6667, password);
        joinChannel(channel);
    }

}
