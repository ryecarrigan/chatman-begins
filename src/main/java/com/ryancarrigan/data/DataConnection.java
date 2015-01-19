package com.ryancarrigan.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * Created by Suave Peanut on 2015.1.14.
 */
public class DataConnection {

    private static final Logger     logger     = LoggerFactory.getLogger(DataConnection.class);
    private static final Properties queries    = loadProperties("query.properties");
    private static final Properties properties = System.getProperties();

    private Connection connection;
    private Properties credentials = new Properties();
    private String     host;

    DataConnection() {
        final String database = properties.getProperty("chatman.database", "localhost");
        final String dbHost = properties.getProperty("chatman.dbhost", "localhost");
        this.host = String.format("jdbc:mysql://%s:3306/%s?useUnicode=true&characterEncoding=utf8", dbHost, database);

        final String dbPass = properties.getProperty("chatman.dbpass", "ch4tp455");
        final String dbUser = properties.getProperty("chatman.dbuser", "chatman");
        this.credentials.put("user",     dbUser);
        this.credentials.put("password", dbPass);
    }

    private void connect() {
        try {
            this.connection = DriverManager.getConnection(this.host, this.credentials);
        } catch (final SQLException sqle) {
            throw new IllegalStateException("Unable to connect to database.", sqle);
        }
    }

    public void disconnect() {
        try {
            if (this.connection != null && this.connection.isValid(2)) this.connection.close();
        } catch (final SQLException sqle) {
            logger.error("Unable to disconnect from database.", sqle);
        }
    }

    private Statement createStatement() throws SQLException {
        return this.connection.createStatement();
    }

    ResultSet query(final String key, final Object... args) {
        final String command = String.format(queries.getProperty(key), args);
        try {
            connect();
            return createStatement().executeQuery(command);
        } catch (final SQLException sqle) {
            logger.info(command);
            logger.error("Error executing query on database.", sqle);
        }
        return null;
    }

    int update(final String key, final Object... args) {
        final String command = String.format(queries.getProperty(key), args);
        try {
            connect();
            return createStatement().executeUpdate(command);
        } catch (final SQLException sqle) {
            logger.info(command);
            logger.error("Error executing update on database.", sqle);
        } finally {
            disconnect();
        }
        return -1;
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
        } catch (final IOException ioe) {
            logger.error("Error loading properties file.", ioe);
        }
        return properties;
    }

}
