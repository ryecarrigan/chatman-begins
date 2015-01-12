package com.ryancarrigan.data;

import com.ryancarrigan.chatman.Action;
import com.ryancarrigan.chatman.Event;
import com.ryancarrigan.chatman.Nick;
import com.ryancarrigan.chatman.Reaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Suave Peanut on 2015.1.10.
 */
public class Datman {

    private static Datman     datman = null;
    private static Logger     logger;
    private static Properties queries;
    private static String     botName;
    private static String     channel;
    private static String     database;
    private static String     dbPass;
    private static String     dbUser;

    static {
        final Properties properties = System.getProperties();
        botName  = properties.getProperty("chatman.botname");
        channel  = properties.getProperty("chatman.channel");
        database = properties.getProperty("chatman.database");
        dbPass   = properties.getProperty("chatman.dbpass");
        dbUser   = properties.getProperty("chatman.dbuser");
        logger   = LoggerFactory.getLogger(Datman.class);
        queries  = loadProperties("query.properties");
    }

    private Connection connection;
    private Properties credentials = new Properties();

    private Datman() {
        setAll();
        connect();
        createTables();
    }

    private void createTables() {
        update("CreateEventTable", channel);
        update("CreateUserTable",  channel);
    }

    private void connect() {
        final String host = String.format("jdbc:mysql://localhost:3306/%s?characterEncoding=UTF-8", database);
        try {
            this.connection = DriverManager.getConnection(host, this.credentials);
        } catch (final SQLException e) {
            throw new IllegalStateException("Unable to connect to database.", e);
        }
    }

    private void disconnect() {
        try {
            if (this.connection != null && this.connection.isValid(2)) this.connection.close();
        } catch (final SQLException e) {
            throw new IllegalStateException("Unable to close database connection.", e);
        }
        datman = null;
    }

    public static void quit() {
        getInstance().disconnect();
    }

    private void setAll() {
        if (channel == null) {
            throw new IllegalArgumentException("The chatman.channel property has not been set.");
        } else if (database == null) {
            throw new IllegalArgumentException("The chatman.database property has not been set.");
        } else if (dbPass == null) {
            throw new IllegalArgumentException("The chatman.dbpass property has not been set.");
        } else if (dbUser == null) {
            throw new IllegalArgumentException("The chatman.dbuser property has not been set.");
        }
        this.credentials.put("user",     dbUser);
        this.credentials.put("password", dbPass);
    }

    private ResultSet query(final String key, final Object... args) {
        final String command = String.format(queries.getProperty(key), args);
        try {
            return this.connection.createStatement().executeQuery(command);
        } catch (final SQLException e) {
            throw new IllegalStateException("Error executing query on database.", e);
        }
    }

    private int update(final String key, final Object... args) {
        final String command = String.format(queries.getProperty(key), args);
        try {
            return this.connection.createStatement().executeUpdate(command);
        } catch (final SQLException e) {
            throw new IllegalStateException("Error executing update on database.", e);
        }
    }

    private static Datman getInstance() {
        if (null == datman)
            datman = new Datman();
        return datman;
    }

    private static Properties loadProperties(final String fileName) {
        final Properties properties = new Properties();
        try {
            final InputStream inputStream = ClassLoader.getSystemResourceAsStream(fileName);
            if (inputStream != null) {
                properties.load(inputStream);
                inputStream.close();
            } else {
                throw new IllegalArgumentException("Input stream was null. Check the queries file.");
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static String getIrcPassword(final String login) {
        final ResultSet password = getInstance().query("GetPassword", login);
        try {
            if (password.next()) {
                return password.getString("Pass");
            } else {
                throw new IllegalArgumentException("No password for IRC user: " + login);
            }
        } catch (final SQLException e) {
            throw new IllegalStateException("Error querying database for password.", e);
        }
    }

    public static List<Reaction> getReactions(final Action action) {
        final List<Reaction> reactionList = new ArrayList<>();
        try {
            final ResultSet resultSet = getInstance().query("GetReactions", action.getEventName(), channel, botName);
            while (resultSet.next()) {
                final Reaction newReaction = new Reaction(
                        resultSet.getString("ReactionType"),
                        resultSet.getString("Nick"),
                        resultSet.getString("Trigger"),
                        resultSet.getString("Reaction"),
                        resultSet.getInt("Count")
                );
                reactionList.add(newReaction);
            }
        } catch (final SQLException sqle) {
            sqle.printStackTrace();
        }
        return reactionList;
    }

    public static Map<String, String> getTwitterCredentials(final String login) {
        final ResultSet twitter = getInstance().query("GetTwitter", login);
        try {
            if (twitter.next()) {
                final Map<String, String> credentials = new HashMap<>();
                credentials.put("ConsumerKey",    twitter.getString("ConsumerKey"));
                credentials.put("ConsumerSecret", twitter.getString("ConsumerSecret"));
                credentials.put("AccessToken",    twitter.getString("AccessToken"));
                credentials.put("AccessSecret",   twitter.getString("AccessSecret"));
                return credentials;
            } else {
                throw new IllegalArgumentException("No credentials for Twitter user: " + login);
            }
        } catch (final SQLException e) {
            throw new IllegalStateException("Error querying database for Twitter credentials.", e);
        }
    }

    public static void insertEvents(final List<Event> events) {
        getInstance().update("InsertEvents", channel, valuesToString(events));
    }

    public static void setBotStatus(final int status) {
        getInstance().update("UpdateBotStatus", channel, status, status);
    }

    public static void setNickStatus(final String nick, final String online) {
        setNickStatus(Arrays.asList(new Nick(nick)), online);
    }

    public static void setNickStatus(final List<Nick> nicks, final String online) {
        getInstance().update("UpdateNickStatus", channel, valuesToString(nicks, online), online);
    }

    private static String valuesToString(final Collection<?> collection, final Object... args) {
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
