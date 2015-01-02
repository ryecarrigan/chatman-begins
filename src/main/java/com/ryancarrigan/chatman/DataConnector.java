package com.ryancarrigan.chatman;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Suave Peanut on 2014.12.30.
 */
class DataConnector {

    private Connection connection;
    private Logger     log = LoggerFactory.getLogger(DataConnector.class);
    private String     table;

    DataConnector(final String channel) {
        Matcher matcher = Pattern.compile("#([A-Za-z]+)").matcher(channel);
        if (matcher.find()) {
            this.table = "EventLog_" + matcher.group().replace("#", "");
        } else {
            throw new IllegalArgumentException("Channel '" + channel + "' do doesn't start with #.");
        }
    }

    void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatman_db", "chatman", "ch4tp455");
        log.info("Connected to database");
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

    void insert(final Event event) throws SQLException {
        event.log();
        final String sql = event.getStatement(table).replace("'", "\'");
        log.info(sql);
        connection.createStatement().executeUpdate(sql);
    }

    void react(final Reaction reaction) throws SQLException {
        final String sql = reaction.getStatement();
        log.info(sql);
        final ResultSet resultSet = connection.createStatement().executeQuery(reaction.getStatement());
        while(resultSet.next()) {
            log.info(resultSet.getString("Data"));
        }

    }

}
