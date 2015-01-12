package com.ryancarrigan.chatman;

import com.ryancarrigan.irksome.DataLogger;
import com.ryancarrigan.irksome.Reactor;
import com.ryancarrigan.irksome.UserLogger;
import com.ryancarrigan.peabot.Peabot;

/**
 * Created by Suave Peanut on 2015.1.6.
 */
public class IrkLauncher {

    public static void main(final String[] args) {
        final Irksome irksome = buildIrkBot(args[0]);
        prepareForImpact(irksome);
        irksome.start();
    }

    private static void prepareForImpact(final Irksome irksome) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                irksome.quit();
            }
        }));
    }

    private static Irksome buildIrkBot(final String mode) {
        final String channel = System.getProperty("chatman.channel"),
                     nick    = System.getProperty("chatman.botname");
        if (channel.length() < 2 || nick.length() < 2) {
            throw new IllegalArgumentException("Double-check your channel and bot name.");
        }
        switch (mode) {
            case "all":
                return new Peabot(channel, nick);
            case "log":
                return new DataLogger(channel, nick);
            case "user":
                return new UserLogger(channel, nick);
            case "react":
                return new Reactor(channel, nick);
            default:
                throw new IllegalArgumentException("Illegal command: " + mode);
        }
    }

}
