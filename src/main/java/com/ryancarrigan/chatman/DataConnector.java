package com.ryancarrigan.chatman;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Suave Peanut on 2014.12.30.
 */
public class DataConnector {

    private String     channel;
    private Connection connection;
    private String     database;
    private Properties credentials = new Properties();
    private String     table;

    public DataConnector(final String database, final String user, final String pass, final String channel) {
        credentials.put("user", user);
        credentials.put("password", pass);
        this.channel  = channel;
        this.database = database;
        this.table    = "EventLog_" + channel;
    }

    public void connect() throws SQLException {
        connect("localhost");
    }

    public void connect(final String host) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:mysql://" + host + ":3306/" + database + "?characterEncoding=UTF-8", credentials);
        this.setBotStatus(1);
        createTable();
    }

    void createTable() throws SQLException {
        final String sql = "CREATE TABLE IF NOT EXISTS `" + table + "` (\n" +
                "`QueueKey` int(10) unsigned PRIMARY KEY AUTO_INCREMENT,\n" +
                "  `EventName` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '',\n" +
                "  `Channel` varchar(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,\n" +
                "  `Login` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,\n" +
                "  `HostName` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,\n" +
                "  `Nick` varchar(128) COLLATE utf8_unicode_ci DEFAULT NULL,\n" +
                "  `Target` varchar(128) COLLATE utf8_unicode_ci DEFAULT NULL,\n" +
                "  `Data` mediumtext COLLATE utf8_unicode_ci,\n" +
                "  `Number` int(16) DEFAULT NULL,\n" +
                "  `Timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
        connection.createStatement().executeUpdate(sql);
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            this.setBotStatus(0);
            connection.close();
        }
    }

    public String getPassword(final String login) throws SQLException {
        final ResultSet query = connection.createStatement().executeQuery("SELECT Pass FROM Credentials WHERE Login='" + login + "'");
        if (query.next()) {
            return query.getString("Pass");
        } else {
            throw new IllegalArgumentException("No password found for user: " + login);
        }
    }

    public void insert(final Event event) {
        final String sql = event.getStatement(table);
        try {
            connection.createStatement().executeUpdate(sql);
        } catch (SQLException e) {
            throw new IllegalArgumentException("Unable to insert event into database:\n" + event.toString(), e);
        }
    }

    public List<Reaction> react(final Action action) {
        final List<Reaction> reactionList = new ArrayList<>();
        try {
            final ResultSet resultSet = connection.createStatement().executeQuery(action.getStatement());
            while (resultSet.next()) {
                final Reaction newReaction = new Reaction(
                        resultSet.getString("Channel"),
                        resultSet.getString("Action"),
                        resultSet.getString("EventName"),
                        resultSet.getString("Nick"),
                        resultSet.getString("Target"),
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

    void setBotStatus(final int online) {
        final String sql = String.format(
                "INSERT INTO `Channels` (Channel, OnlineStatus) " +
                        "VALUES ('%s', %d) " +
                        "ON DUPLICATE KEY " +
                        "UPDATE OnlineStatus=%d;", this.channel, online, online);
        try {
            connection.createStatement().executeUpdate(sql);
        } catch (final SQLException sqle) {
            throw new IllegalArgumentException("Unable to insert bot status into database: " + online);
        }
    }

}
