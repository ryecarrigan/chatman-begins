package com.ryancarrigan.chatman;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Suave Peanut on 2014.12.30.
 */
class DataConnector {

    private Connection connection;
    private String     database;
    private Logger     log = LoggerFactory.getLogger(DataConnector.class);
    private Properties properties = new Properties();
    private String     table;

    DataConnector(final String channel) {
        final Matcher matcher = Pattern.compile("#([A-Za-z]+)").matcher(channel);
        if (matcher.find()) {
            this.table = "EventLog_" + matcher.group().replace("#", "");
        } else {
            throw new IllegalArgumentException("Channel '" + channel + "' do doesn't start with #.");
        }
    }

    DataConnector(final String database, final String user, final String pass, final String table) {
        properties.put("user", user);
        properties.put("password", pass);
        this.database = database;
        this.table    = "EventLog_" + table;
    }

    void connect() throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + database, properties);
        createTable();
    }

    void createTable() throws SQLException {
        final String smt= "CREATE TABLE IF NOT EXISTS `" + table + "` (\n" +
                "`QueueKey` int(10) unsigned PRIMARY KEY AUTO_INCREMENT,\n" +
                "  `EventName` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '',\n" +
                "  `Channel` varchar(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,\n" +
                "  `Login` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,\n" +
                "  `HostName` varchar(256) COLLATE utf32_unicode_ci DEFAULT NULL,\n" +
                "  `Nick` varchar(128) COLLATE utf32_unicode_ci DEFAULT NULL,\n" +
                "  `Target` varchar(128) COLLATE utf32_unicode_ci DEFAULT NULL,\n" +
                "  `Data` mediumtext COLLATE utf32_unicode_ci,\n" +
                "  `Number` int(16) DEFAULT NULL,\n" +
                "  `Timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=22883 DEFAULT CHARSET=utf32 COLLATE=utf32_unicode_ci;";
        connection.createStatement().executeUpdate(smt);
        log.info("Using table: " + table);
    }

    void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    String getPassword(final String login) throws SQLException {
        final String sql = "SELECT Pass FROM Credentials WHERE Login='" + login + "'";
        ResultSet query = connection.createStatement().executeQuery(sql);
        if (query.next()) {
            return query.getString("Pass");
        } else {
            throw new IllegalArgumentException("No password found for user: " + login);
        }
    }

    void insert(final Event event) {
        final String sql = event.getStatement(table).replace("'", "\'");
        try {
            connection.createStatement().executeUpdate(sql);
        } catch (SQLException e) {
            log.error("Error inserting event: " + e.getMessage());
        }
    }

    List<Reaction> react(final Action reaction) {
        final List<Reaction> reactionList = new ArrayList<>();
        try {
            final ResultSet resultSet = connection.createStatement().executeQuery(reaction.getStatement());
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reactionList;
    }
}
