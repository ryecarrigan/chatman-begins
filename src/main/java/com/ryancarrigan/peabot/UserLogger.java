package com.ryancarrigan.peabot;

import com.ryancarrigan.chatman.EventName;
import com.ryancarrigan.chatman.IrcBot;
import com.ryancarrigan.chatman.Nick;
import org.jibble.pircbot.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Suave Peanut on 2015.1.20.
 */
public class UserLogger extends IrcBot {

    private static final Logger                   logger;
    private static final ScheduledExecutorService executor;

    static {
        executor = Executors.newScheduledThreadPool(4);
        logger   = LoggerFactory.getLogger(Peabot.class);
    }

    private NickService joinQueue;
    private NickService partQueue;

    public UserLogger(final String channel, final String login) {
        super(channel, login);

        // Define a runnable that can be executed on a schedule.
        this.joinQueue = new NickService(login, channel, true);
        this.partQueue = new NickService(login, channel, false);
    }

    @Override
    public void quit() {
        // Cancel the running service.
        executor.shutdown();
        joinQueue.dumpQueue();
        partQueue.dumpQueue();

        // And disconnect from the IRC server.
        disconnect();

        // Disconnecting would result in unknown user states. Therefore, set everybody offline.
        getDataman().setAllOffline();
    }

    /**
     * Checks the event type and updates the Users table on Join or Part.
     * @param event IrcEvent
     * @param nick  String
     */
    @Override
    protected void receiveEvent(final EventName event, final String nick, final String data) {
        switch (event) {
            case JOIN:
                // On Join, set the user to online.
                updateUser(nick, true);
                break;
            case PART:
                // On Part, set the user to offline.
                updateUser(nick, false);
                break;
            default:
                // Otherwise, do nothing.
                break;
        }
    }

    @Override
    public void start() {
        scheduleUserServices();

        // Request the OAuth credentials from the database.
        final String password = getDataman().getIrcPassword(getName());

        // Join the configured IRC channel.
        join(password);
    }

    /**
     * Schedule joins and parts to be logged alternately every 5 seconds.
     */
    private void scheduleUserServices() {
        executor.scheduleAtFixedRate(joinQueue, 0,    5000, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(partQueue, 2500, 5000, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onUserList(final String channel, final User[] users) {
        // First, set everybody in the channel to offline.
        getDataman().setAllOffline();

        // Then, set everybody currently in the channel to online.
        updateUsers(users, true);
    }

    /**
     * Updates the online status of a single nick.
     * @param user      String
     * @param online    String
     */
    private void updateUser(final String user, final boolean online) {
        // Create a Nick for the user to update
        final Nick nick = new Nick(user, online);

        // Add it to either the join or part queue.
        if (online) {
            joinQueue.add(nick);
        } else {
            partQueue.add(nick);
        }
    }

    /**
     * Updates an array of users to the same online/offline status.
     * @param users     User[]
     * @param online    String
     */
    private void updateUsers(final User[] users, final boolean online) {
        // For each User in users, send it to the join/part queues.
        for (final User user : users) {
            updateUser(user.getNick(), online);
        }
    }

}

