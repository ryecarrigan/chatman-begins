package com.ryancarrigan.chatman;

import com.ryancarrigan.irksome.DataLogger;
import com.ryancarrigan.peabot.Peabot;

/**
 * Created by Suave Peanut on 2015.1.6.
 */
public class IrkLauncher {

    public static void main(final String[] args) {
        if (args.length < 3) {
            throw new IllegalArgumentException("Not enough arguments. <channel name> <bot name> <log/react>");
        } else {
            System.out.println(String.format("Starting bot %s in channel #%s in %s mode.", args[1], args[0], args[2]));
        }

        final Irksome irksome = buildIrkBot(args);
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

    private static Irksome buildIrkBot(final String[] args) {
        String db = "chatman_db", dbLogin = "chatman", dbPass = "ch4tp455";
        switch (args[2]) {
            case "log":
                return new DataLogger(args[0], args[1], db, dbLogin, dbPass);
            case "react":
                return new Peabot(args[0], args[1], db, dbLogin, dbPass);
            default:
                throw new IllegalArgumentException("Illegal command: " + args[2]);
        }
    }

}
