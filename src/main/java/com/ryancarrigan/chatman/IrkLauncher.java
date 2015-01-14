package com.ryancarrigan.chatman;

import com.ryancarrigan.peabot.Botmode;
import com.ryancarrigan.peabot.Peabot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Suave Peanut on 2015.1.6.
 */
public class IrkLauncher {

    private static final Logger logger = LoggerFactory.getLogger(IrkLauncher.class);

    public static void main(final String[] args) {
        loadProperties("config.properties");
        final IrcBot ircBot = buildIrkBot();
        prepareForImpact(ircBot);
        ircBot.start();
    }

    private static Properties loadProperties(final String fileName) {
        final Properties properties = new Properties();
        try {
            final InputStream inputStream = new FileInputStream(fileName);
            System.getProperties().load(inputStream);
            inputStream.close();
        } catch (final IOException e) {
            throw new RuntimeException("Config file not found.");
        }
        return properties;
    }

    private static void prepareForImpact(final IrcBot ircBot) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                ircBot.quit();
            }
        }));
    }

    private static IrcBot buildIrkBot() {
        final String channel = System.getProperty("chatman.channel");
        final String nick    = System.getProperty("chatman.botname");
        final String mode    = System.getProperty("chatman.mode");
        logger.info("Starting new bot in mode: " + mode);

        Botmode botmode;
        switch (mode) {
            case "all":
                botmode = Botmode.ALL;
                break;
            case "log":
                botmode = Botmode.LOG;
                break;
            case "user":
                botmode = Botmode.USER;
                break;
            case "react":
                botmode = Botmode.REACT;
                break;
            default:
                botmode = Botmode.LOG;
        }
        return new Peabot(channel, nick, botmode);
    }

}
