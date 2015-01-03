package com.ryancarrigan.chatman;

/**
 * Created by Suave Peanut on 2015.1.3.
 */
public class PeabotLauncher {

    public static void main(final String[] args) {
        if (args.length < 1) {
            System.out.println("Please provide a channel name.");
            System.exit(-1);
        } else {
            final String channel = args[0];
            final Peabot peabot = new Peabot(channel, "suavepeabot");
            prepareForImpact(peabot);
            peabot.connectToChannel();
        }
    }

    private static void prepareForImpact(final ChatBot chatBot) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                chatBot.quit();
            }
        }));
    }

}
