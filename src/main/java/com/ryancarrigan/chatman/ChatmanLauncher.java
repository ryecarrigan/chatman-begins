package com.ryancarrigan.chatman;

import org.jibble.pircbot.IrcException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Created by Suave Peanut on 2014.12.27.
 */
public class ChatmanLauncher {

    public static void main(final String[] args) {
        System.out.println("ARGS: " + Arrays.toString(args));

        String channel = "#";
        if (args.length < 1) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter name of the channel to join: ");
            try {
                channel = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            channel = channel + args[0];
        }

        final Chatman chatman = new Chatman("SuavePeabot", "oauth:b5ygqi63qb6fb92gacu57lb1fblw3j", channel);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                chatman.quit();
            }
        }));

        try {
            chatman.connect();
            chatman.joinChannel(channel);
        } catch (IOException e) {
            System.exit(1);
        } catch (IrcException e) {
            System.exit(2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
