package com.ryancarrigan.peabot;

import com.ryancarrigan.chatman.IrcBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class PeabotLauncher {

    private static final Logger logger = LoggerFactory.getLogger(PeabotLauncher.class);

    /**
     * Starts the Peabot!
     * @param args  String[] - the command line arguments
     */
    public static void main(final String[] args) {
        // See if the properties file is overridden by the command line.
        final String propertiesFile = System.getProperty("chatman.propfile", "config.properties");

        // And load the accompanying properties file.
        loadProperties(propertiesFile);

        // Then build the bot using the system properties.
        final IrcBot bot = buildBot();
        // And set the shutdown hook.
        prepareForImpact(bot);

        // Finally start the bot.
        bot.start();
    }

    /**
     * Builds the IRC bot from the input properties.
     * @return IrcBot
     */
    private static IrcBot buildBot() {
        // Load the bot's nick and the destination channel.
        final String channel = System.getProperty("chatman.channel");
        final String nick    = System.getProperty("chatman.botname");

        // Load the bot mode property.
        final String mode = System.getProperty("chatman.mode");
        switch (mode) {
            case "log":
                return new Peabot(channel, nick, Botmode.LOG);
            case "react":
                return new Peabot(channel, nick, Botmode.REACT);
            default:
                return new Peabot(channel, nick, Botmode.ALL);
        }
    }

    /**
     * Loads the properties file from the same directory as the JAR file.
     * @param fileName  String
     */
    private static void loadProperties(final String fileName) {
        try {
            // Declare an InputStream for the filename.
            final InputStream inputStream = new FileInputStream(fileName);

            // Load that file into system properties.
            System.getProperties().load(inputStream);

            // Close the stream.
            inputStream.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a shutdown hook to the IRC bot.
     * @param bot       IrcBot
     */
    private static void prepareForImpact(final IrcBot bot) {
        // Create a runnable to quit the IrcBot on shutdown.
        final Runnable impact = new Runnable() {
            @Override
            public void run() {
                bot.quit();
            }
        };

        // Add the runnable as a shutdown hook thread.
        Runtime.getRuntime().addShutdownHook(new Thread(impact));
    }

}
