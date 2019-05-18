package com.ryancarrigan.peabot;

import com.ryancarrigan.chatman.Event;
import com.ryancarrigan.chatman.EventName;
import com.ryancarrigan.chatman.IrcBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Suave Peanut on 2015.1.20.
 */
public class EventLogger extends IrcBot {

    private static final Logger logger;
    private static final ScheduledExecutorService executor;

    static {
        executor = Executors.newSingleThreadScheduledExecutor();
        logger   = LoggerFactory.getLogger(Peabot.class);
    }

    private EventService loggingQueue;
    private Future<?> logSchedule;

    public EventLogger(final String channel, final String login) {
        super(channel, login);

        // Define a runnable that can be executed on a schedule.
        this.loggingQueue = new EventService(login, channel);
    }

    private void scheduleLogService() {
        this.logSchedule = executor.scheduleAtFixedRate(loggingQueue, 0, 2, TimeUnit.SECONDS);
    }

    @Override
    public void quit() {
        // Cancel the running service.
        executor.shutdown();
        loggingQueue.dumpQueue();

        // And disconnect from the IRC server.
        disconnect();
    }

    @Override
    protected void receiveEvent(final EventName eventName, final String nick, final String data) {
        // Build a new event from the input parameters.
        final Event event = new Event(eventName, nick, data);

        // Log the incoming event, maybe?
        switch (eventName) {
            case MESSAGE:
                logger.info(String.format("[%s] %s", nick, data));
                break;
            default:
                break;
        }

        // Determine handling of the event based on the bot mode.
        logEvent(event);
    }

    @Override
    public void start() {
        scheduleLogService();

        // Request the OAuth credentials from the database.
        final String password = getDataman().getIrcPassword(getName());

        // Join the configured IRC channel.
        join(password);
        logger.info(String.format("Starting %s in %s", getName(), getChannel()));
    }

    /**
     * Enqueues an event to be stored in the event log.
     * @param event Event
     */
    private void logEvent(final Event event) {
        loggingQueue.add(event);

        // Backup strats if the log starts to get clogged?
        final int size = loggingQueue.size();
        if (size > 500) {
            logger.info(String.format("Logging queue is unusually large (%d). Restarting service.", size));
            loggingQueue.run();
            scheduleLogService();
        }
    }

}
