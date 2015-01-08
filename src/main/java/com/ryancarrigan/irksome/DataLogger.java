package com.ryancarrigan.irksome;

import com.ryancarrigan.chatman.DataConnector;
import com.ryancarrigan.chatman.Event;
import com.ryancarrigan.chatman.IrkEvent;
import com.ryancarrigan.chatman.Irksome;
import org.jibble.pircbot.IrcException;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by Suave Peanut on 2015.1.6.
 */
public class DataLogger extends Irksome {

    private DataConnector dataConnector;

    public DataLogger(final String channel, final String login, final String databaseName,
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
    public void receiveEvent(final IrkEvent event, final String channel, final String login, final String hostName, final String nick, final String target, final String data, final Number number) {
        final Event irk = new Event(event, channel, login, hostName, nick, target, data, number);
        this.dataConnector.insert(irk);
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
    }

}
