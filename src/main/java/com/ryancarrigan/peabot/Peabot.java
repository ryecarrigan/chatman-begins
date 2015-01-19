package com.ryancarrigan.peabot;

import com.ryancarrigan.chatman.*;
import com.ryancarrigan.data.Dataman;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Suave Peanut on 2015.1.6.
 */
public class Peabot extends IrcBot {

    private static final Logger logger = LoggerFactory.getLogger(Peabot.class);
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(8);

    private final Botmode  botmode;
    private Dataman        dataman;
    private EventLogger    loggingQueue;
    private MessageService messageQueue;
    private NickService    joinQueue;
    private NickService    partQueue;
    private Twitter        twitter;
    private ServiceQueue[] queues;

    private Future<?> logSchedule;
    private Future<?> messageSchedule;
    private Future<?> joinSchedule;
    private Future<?> partSchedule;

    public Peabot(final String channel, final String login, final Botmode botmode) {
        super(channel, login);
        this.botmode = botmode;
        this.dataman = new Dataman(login, channel, true);

        // Define a runnable that can be executed on a schedule.
        this.loggingQueue = new EventLogger(login, channel);
        this.messageQueue = new MessageService(this);
        this.joinQueue    = new NickService(login, channel, true);
        this.partQueue    = new NickService(login, channel, false);
        this.queues       = new ServiceQueue[] { loggingQueue, messageQueue, joinQueue, partQueue };
    }

    /**
     * Start the necessary services and join the configured channel.
     */
    @Override
    public void start() {
        // Schedule the message/logging services and log in to Twitter if reactions are enabled.
        switch (botmode) {
            case ALL:
                // For ALL mode, schedule the log and message services and set Twitter.
                scheduleLogService();
                scheduleUserServices();
            case REACT:
                // For REACT mode, don't schedule logging.
                scheduleMessageService();
                setTwitter();
                break;
            case LOG:
                // For LOG mode, only schedule logging.
                scheduleLogService();
                scheduleUserServices();
                break;
        }
        scheduleQueueReport();

        try {
            // Join the configured IRC channel.
            joinChannel();

            // Once we're in, set this bot's status to online for that channel.
            dataman.setBotStatus(1);
        } catch (IrcException | IOException e) {
            throw new RuntimeException(e);
        }

        logger.info(String.format("Starting %s in %s", getName(), getChannel()));
    }

    private void scheduleLogService() {
        this.logSchedule = executor.scheduleAtFixedRate(loggingQueue, 0, 2, TimeUnit.SECONDS);
    }

    private void scheduleMessageService() {
        this.messageSchedule = executor.scheduleAtFixedRate(messageQueue, 0, 800, TimeUnit.MILLISECONDS);
    }

    private void scheduleQueueReport() {
        // Define a runnable that can be executed on a schedule.
        final Runnable queueReporter = new Runnable() {
            @Override
            public void run() {
                final int logSize = loggingQueue.size();
                final String messages = String.format(
                        "<%s on %s>\tL:%d\tM:%d\tJ:%d\tP:%d", getName(), getChannel(),
                        logSize, messageQueue.size(), joinQueue.size(), partQueue.size());

                // More backup strats for bloated logging.
                if (logSize > 250) {
                    loggingQueue.run();
                }
                logger.info(messages);
            }
        };

        // Show queue status once every few minutes.
        executor.scheduleAtFixedRate(queueReporter, 5, 5, TimeUnit.MINUTES);
    }

    /**
     * Schedule joins and parts to be logged alternately every 15 seconds.
     */
    private void scheduleUserServices() {
        this.joinSchedule = executor.scheduleAtFixedRate(joinQueue, 0,  30, TimeUnit.SECONDS);
        this.partSchedule = executor.scheduleAtFixedRate(partQueue, 15, 30, TimeUnit.SECONDS);
    }

    /**
     * Shut down gracefully by disconnecting from the database and leaving the IRC server.
     */
    @Override
    public void quit() {
        switch (botmode) {
            // If we are logging, then disconnecting would result in unknown user states.
            // Therefore, set everybody offline.
            case ALL:
            case USER:
                dataman.setAllOffline();
                break;
        }

        // Dump all un-logged events into a file.
        for (final ServiceQueue queue : queues) queue.dumpQueue();
        executor.shutdown();

        // Record this bot offline for this channel.
        dataman.setBotStatus(0);

        // And disconnect from the IRC server.
        disconnect();
    }

    /**
     * Handles events of every type.
     * @param eventName String
     * @param nick      String
     * @param data      String
     */
    @Override
    public void receiveEvent(final EventName eventName, final String nick, final String data) {
        // Build a new event from the input parameters.
        final Event event = new Event(eventName, nick, data);

        // Determine handling of the event based on the bot mode.
        switch (botmode) {
            case ALL:
                // For ALL mode, log the event and react to it.
                logEvent(event);
                setUserStatus(eventName, nick);
            case REACT:
                // For REACT mode, react but do not log.
                determineReaction(event);
                break;
            case LOG:
                // For LOG mode, log but do not react.
                logEvent(event);
                setUserStatus(eventName, nick);
                break;
        }
    }

    /**
     * Enqueues an event to be stored in the event log.
     * @param event Event
     */
    private void logEvent(final Event event) {
        loggingQueue.add(event);

        // Backup strats if the log starts to get clogged?
        if (loggingQueue.size() > 250) {
            logger.info("Logging queue is filling up. Checking whether queue is still running...");
            loggingQueue.run();
            restartLoggingQueue();
        }
    }

    private void restartLoggingQueue() {
        boolean isCancelled = this.logSchedule.isCancelled();
        boolean isDone      = this.logSchedule.isDone();
        if (isCancelled || isDone) {
            logger.info(String.format("Restarting log scheduler. (Done: %b, Cancelled:%b)", isCancelled, isDone));
            scheduleLogService();
        }
    }

    /**
     * Checks the event type and updates the Users table on Join or Part.
     * @param event IrcEvent
     * @param nick  String
     */
    private void setUserStatus(final EventName event, final String nick) {
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

    /**
     * When a user list is received, if logging is enabled then update the Users table.
     * @param channel   String
     * @param users     User[]
     */
    @Override
    protected void onUserList(final String channel, final User[] users) {
        // Process the event as usual.
        super.onUserList(channel, users);

        // If we're logging events, then update the user table.
        switch (botmode) {
            case ALL:
            case LOG:
                // First, set everybody in the channel to offline.
                dataman.setAllOffline();
                // Then, set everybody currently in the channel to online.
                updateUsers(users, true);
                break;
            default:
                // Otherwise, do nothing.
                break;
        }
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

    /**
     * Loads the OAuth "password" for the desired Twitch username, then connects to Twitch IRC and
     * joins the configured channel.
     * @throws IrcException
     * @throws IOException
     */
    private void joinChannel() throws IrcException, IOException {
        // Request the OAuth credentials from the database.
        final String password = dataman.getIrcPassword(getName());

        // Connect the Twitch IRC server using the password.
        connect("irc.twitch.tv", 6667, password);

        // Join the pre-configured IRC channel.
        joinChannel(getChannel());
    }

    /**
     * Performs a reaction to an IRC event.
     * @param action    Action
     * @param reaction  Reaction
     */
    private void react(final Action action, final Reaction reaction) {
        // Switch on the reaction type.
        final String type = reaction.getReactionType();
        switch (type) {
            case "Message":
                // Iterate over the number of messages configured for the reaction.
                final int count = reaction.getCount();
                for (int i = 0; i < count; ++i) {
                    final String[] reactions = reaction.getReaction().split("\\|\\|");
                    Collections.addAll(messageQueue.getQueue(), reactions);
                }
                break;
            case "Tweet":
                // Tweets are sent immediately and only once.
                tweet(action, reaction);
                break;
            default:
                // For all other reaction type, notify and do nothing else.
                logger.error("Reaction is of an unknown reaction type.");
                break;
        }
    }

    /**
     * Convert and Event into and Action and query for its Reactions. If possible reactions are
     * found, check them for matching regex and nicks and react if they match.
     * @param event Event
     */
    private void determineReaction(final Event event) {
        // Convert the Event into an Action object.
        final Action command = new Action(event);

        // Query the database for reaction to this action.
        final List<Reaction> reactions = dataman.getReactions(command);

        // Iterate over reactions, checking each for matching regex and nicks.
        for (final Reaction reaction : reactions) {
            if (reaction.isTriggeredBy(command)) {
                // If the reaction should be triggered by the action, then react.
                react(command, reaction);
            }
        }
    }

    /**
     * Sets the Twitter object using the Twitter credentials for this bot name.
     */
    private void setTwitter() {
        // Load a HashMap of Twitter credentials for this bot name.
        final Map<String, String> credentials = dataman.getTwitterCredentials(getName());

        // Verify that we receive credentials for the configurated account.
        if (!credentials.isEmpty()) {
            // Construct a new Twitter Configuration with the returned credentials.
            final Configuration configuration = new ConfigurationBuilder()
                    .setOAuthAccessToken(credentials.get("AccessToken"))
                    .setOAuthAccessTokenSecret(credentials.get("AccessSecret"))
                    .setOAuthConsumerKey(credentials.get("ConsumerKey"))
                    .setOAuthConsumerSecret(credentials.get("ConsumerSecret"))
                    .build();

            // Get a Twitter instance using the built Configuration.
            this.twitter = new TwitterFactory(configuration).getInstance();
        } else {
            logger.error("Could not retrieve credentials for bot: " + getName());
        }
    }

    /**
     * Builds a message from the action and reaction and tweets it out.
     * @param action    Action
     * @param reaction  Reaction
     */
    private void tweet(final Action action, final Reaction reaction) {
        // Check that Twitter has been set.
        if (twitter != null) {
            // Format the reaction message to fit within the character limit.
            final String tweet = tweetFormat(action.getNick(), action.getAction(), reaction.getReaction());
            try {
                // Send the tweet.
                twitter.updateStatus(tweet);
            } catch (final TwitterException te) {
                logger.error(String.format("Error sending tweet: [%s]", tweet), te);
            }
        } else {
            logger.error("Could not send tweet because Twitter was never set.");
        }
    }

    /**
     * Formats the Tweet so that it doesn't exceed 140 characters.
     * @param nick      String
     * @param message   String
     * @param reaction  String
     * @return          String - a formatted message ready to be tweeted
     */
    private String tweetFormat(final String nick, final String message, final String reaction) {
        // Set a message goal of the maximum characters less the included nick and reaction.
        final int messageGoal = 140 - (nick.length() + reaction.length());

        // If the un-formatted message is too long, then abridge it with a trailing ellipsis.
        final String newMessage  = (message.length() > messageGoal)
                ? message.substring(0, messageGoal - 3).trim() + "..."
                : message;

        // Return a String of the form: "Nick: (formatted reaction)"
        return String.format(nick + ": " + reaction, newMessage);
    }

}
