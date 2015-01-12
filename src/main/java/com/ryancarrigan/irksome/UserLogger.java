package com.ryancarrigan.irksome;

import com.ryancarrigan.chatman.IrkEvent;
import com.ryancarrigan.chatman.Irksome;
import com.ryancarrigan.chatman.Nick;
import com.ryancarrigan.data.Datman;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Suave Peanut on 2015.1.6.
 */
public class UserLogger extends Irksome {

    private Logger logger = LoggerFactory.getLogger(UserLogger.class);

    public UserLogger(final String channel, final String login) {
        super(channel, login);
    }

    @Override
    public void quit() {
        Datman.setBotStatus(0);
        Datman.setNickStatus(getNick(), "0");
        Datman.quit();
    }

    @Override
    protected void receiveEvent(final IrkEvent event, final String sourceLogin, final String hostName,
                                final String nick, final String target, final String data, final Number number) {
        switch (event) {
            case JOIN:
                updateUser(nick, "1");
                break;
            case PART:
                updateUser(nick, "0");
                break;
            default:
                break;
        }
    }

    @Override
    protected void onUserList(final String channel, final User[] users) {
        final List<Nick> nicks = new ArrayList<>();
        for (final User user : users) nicks.add(new Nick(user.getNick()));
        Datman.setNickStatus(nicks, "1");
    }

    private void updateUser(final String nick, final String online) {
        Datman.setNickStatus(nick, online);
    }

    @Override
    public void start() {
        try {
            joinChannel();
        } catch (SQLException | IrcException | IOException e) {
            throw new IllegalStateException(e);
        }
        Datman.setBotStatus(1);
    }

    private void joinChannel() throws SQLException, IrcException, IOException {
        final String password = Datman.getIrcPassword(getName());
        this.connect("irc.twitch.tv", 6667, password);
        this.joinChannel(getChannel());
    }

}
