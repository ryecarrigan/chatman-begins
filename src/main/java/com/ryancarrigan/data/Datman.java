package com.ryancarrigan.data;

import com.ryancarrigan.chatman.Action;
import com.ryancarrigan.chatman.Event;
import com.ryancarrigan.chatman.Nick;
import com.ryancarrigan.chatman.Reaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Suave Peanut on 2015.1.10.
 */
public class Datman {

    private static Logger logger = LoggerFactory.getLogger(Datman.class);

    private String         botName;
    private String         channel;

    public Datman(final String botName, final String channel) {
        this.botName    = botName;
        this.channel    = channel.replace("#", "");
        createTables();
    }

    private void createTables() {
        getInstance().update("CreateEventTable", channel);
        getInstance().update("CreateUserTable",  channel);
    }

    private DataConnection getInstance() {
        return new DataConnection();
    }

    public String getIrcPassword(final String login) {
        final DataConnection connection = getInstance();
        final ResultSet password = connection.query("GetPassword", login);
        try {
            if (password != null && password.next()) {
                return password.getString("Pass");
            } else {
                throw new IllegalArgumentException("No password for IRC user: " + login);
            }
        } catch (final SQLException e) {
            throw new IllegalStateException("Error querying database for password.", e);
        } finally {
            connection.disconnect();
        }
    }

    public List<Reaction> getReactions(final Action action) {
        final DataConnection connection = getInstance();
        final List<Reaction> reactionList = new ArrayList<>();
        try {
            final ResultSet resultSet = connection.query("GetReactions", action.getEventName(), channel, botName);
            if (resultSet != null) {
                while (resultSet.next()) {
                    final Reaction newReaction = new Reaction(
                            resultSet.getString("ReactionType"),
                            resultSet.getString("Nick"),
                            resultSet.getString("Regex"),
                            resultSet.getString("Reaction"),
                            resultSet.getInt("Times")
                    );
                    reactionList.add(newReaction);
                }
            }
        } catch (final SQLException sqle) {
            logger.error(sqle.getMessage());
        } finally {
            connection.disconnect();
        }
        return reactionList;
    }

    public Map<String, String> getTwitterCredentials(final String login) {
        final DataConnection connection = getInstance();
        final Map<String, String> credentials = new HashMap<>();
        final ResultSet twitter = connection.query("GetTwitter", login);
        try {
            if (twitter != null && twitter.next()) {
                credentials.put("ConsumerKey",    twitter.getString("ConsumerKey"));
                credentials.put("ConsumerSecret", twitter.getString("ConsumerSecret"));
                credentials.put("AccessToken",    twitter.getString("AccessToken"));
                credentials.put("AccessSecret",   twitter.getString("AccessSecret"));
            } else {
                logger.error("No credentials for Twitter user: " + login);
            }
        } catch (final SQLException sqle) {
            logger.error("Error querying database for Twitter credentials.", sqle);
        } finally {
            connection.disconnect();
        }
        return credentials;
    }

    public void insertEvents(final Collection<Event> events) {
        getInstance().update("InsertEvents", channel, valuesToString(events));
    }

    public void setAllOffline() {
        getInstance().update("SetAllOffline", channel);
    }

    public void setBotStatus(final int status) {
        getInstance().update("UpdateBotStatus", channel, status, status);
    }

    public void setNickStatus(final Collection<Nick> nicks, final String online) {
        getInstance().update("UpdateNickStatus", channel, valuesToString(nicks, online), online);
    }

    private String valuesToString(final Collection<?> collection, final Object... args) {
        final StringBuilder values = new StringBuilder();
        final Iterator<?> iterator = collection.iterator();
        while (iterator.hasNext()) {
            final String next = String.format(iterator.next().toString(), args);
            values.append(next);
            if (iterator.hasNext()) values.append(", ");
        }
        return values.toString();
    }

}
