package com.ryancarrigan.peabot;

import com.ryancarrigan.chatman.*;
import com.ryancarrigan.data.Datman;
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
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Suave Peanut on 2015.1.6.
 */
public class Peabot extends IrcBot {

    private static final Logger logger = LoggerFactory.getLogger(Peabot.class);
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final Botmode botmode;
    private Datman        datman;
    private Deque<Event>  loggingQueue = new LinkedList<>();
    private Queue<String> messageQueue = new LinkedList<>();
    private Queue<Nick>   joinQueue    = new LinkedList<>();
    private Queue<Nick>   partQueue    = new LinkedList<>();
    private Twitter       twitter;

    public Peabot(final String channel, final String login, final Botmode botmode) {
        super(channel, login);
        this.botmode = botmode;
        this.datman  = datman();
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
            datman.setBotStatus(1);
        } catch (IrcException | IOException e) {
            throw new RuntimeException(e);
        }
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
                datman.setAllOffline();
                break;
        }

        // Record this bot offline for this channel.
        datman.setBotStatus(0);

        // And disconnect from the IRC server.
        disconnect();
    }

    private void scheduleLogService() {
        // Define a runnable that can be executed on a schedule.
        final Runnable logging = new Runnable() {
            @Override
            public void run() {
                // Check if there are events waiting to be logged.
                final int size = loggingQueue.size();
                if (size > 0) {
                    // Send all queued events to the database.
                    datman().insertEvents(loggingQueue);
                    // Clear the queue after events have been logged.
                    loggingQueue.clear();
                }
                logger.info(String.format("Recorded %d events.", size));
            }
        };

        // Schedule events to be logged into the database every 5 seconds.
        executor.scheduleAtFixedRate(logging, 0, 5, TimeUnit.SECONDS);
    }

    private void scheduleMessageService() {
        // Define a runnable that can be executed on a schedule.
        final Runnable messaging = new Runnable() {
            @Override
            public void run() {
                // Check if there is a message in the queue.
                final String message = messageQueue.poll();
                if (message != null) {
                    // Send a message to the current channel.
                    sendMessage(getChannel(), message);

                    // Send the newly-created message into the database.
                    final Event event = new Event(EventName.MESSAGE, getNick(), null, getNick(), null, message, null);
                    loggingQueue.add(event);
                }
            }
        };

        // Schedule messages to be removed from the queue every 750ms.
        executor.scheduleAtFixedRate(messaging, 0, 750, TimeUnit.MILLISECONDS);
    }

    private void scheduleQueueReport() {
        // Define a runnable that can be executed on a schedule.
        final Runnable queueReporter = new Runnable() {
            @Override
            public void run() {
                final String messages = String.format(
                        "<QUEUES> L:%d M:%d J:%d P:%d",
                        loggingQueue.size(), messageQueue.size(),joinQueue.size(), partQueue.size());
                logger.info(messages);
            }
        };

        // Show queue status once every few minutes.
        executor.scheduleAtFixedRate(queueReporter, 5, 5, TimeUnit.MINUTES);
    }

    private void scheduleUserServices() {
        // Define a runnable that can be executed on a schedule.
        final Runnable joinLogging = new Runnable() {
            @Override
            public void run() {
                // Check if there are events waiting to be logged.
                if (!joinQueue.isEmpty()) {
                    // Send all queued events to the database.
                    datman().setNickStatus(joinQueue, "1");
                    // Clear the queue after events have been logged.
                    joinQueue.clear();
                }
            }
        };

        final Runnable partLogging = new Runnable() {
            @Override
            public void run() {
                // Check if there are events waiting to be logged.
                if (!partQueue.isEmpty()) {
                    // Send all queued events to the database.
                    datman().setNickStatus(partQueue, "0");
                    // Clear the queue after events have been logged.
                    partQueue.clear();
                }
            }
        };

        // Schedule joins and parts to be logged alternately every 30 seconds.
        executor.scheduleAtFixedRate(joinLogging, 0,  60, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(partLogging, 30, 60, TimeUnit.SECONDS);
    }

    private Datman datman() {
        return new Datman(getName(), getChannel());
    }

    /**
     * Handles events of every type.
     * @param eventName String
     * @param login     String
     * @param hostName  String
     * @param nick      String
     * @param target    String
     * @param data      String
     * @param number    String
     */
    @Override
    public void receiveEvent(final EventName eventName, final String login, final String hostName, final String nick,
                             final String target, final String data, final Number number) {
        // Build a new event from the input parameters.
        final Event event = new Event(eventName, login, hostName, nick, target, data, number);

        // Determine handling of the event based on the bot mode.
        switch (botmode) {
            case ALL:
                // For ALL mode, log the event and react to it.
                loggingQueue.add(event);
                setUserStatus(eventName, nick);
            case REACT:
                // For REACT mode, react but do not log.
                determineReaction(event);
                break;
            case LOG:
                // For LOG mode, log but do not react.
                loggingQueue.add(event);
                setUserStatus(eventName, nick);
                break;
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
                datman.setAllOffline();
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
        final String password = datman.getIrcPassword(getName());

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
                    Collections.addAll(messageQueue, reactions);
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
        final List<Reaction> reactions = datman.getReactions(command);

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
        final Map<String, String> credentials = datman.getTwitterCredentials(getName());

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
