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
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Suave Peanut on 2015.1.6.
 */
public class Peabot extends Irksome {

    private final static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    private Logger        logger = LoggerFactory.getLogger(Peabot.class);
    private List<Event>   loggingQueue = new LinkedList<>();
    private Queue<String> messageQueue = new LinkedList<>();
    private Twitter       twitter;

    public Peabot(final String channel, final String login) {
        super(channel, login);
    }

    private void scheduleMessageService() {
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!loggingQueue.isEmpty()) {
                    Datman.insertEvents(loggingQueue);
                    loggingQueue.clear();
                }
            }
        }, 0, 10, TimeUnit.SECONDS);

        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                final String message = messageQueue.poll();
                if (message != null) {
                    loggingQueue.add(new Event(IrkEvent.MESSAGE, getNick(), null, getNick(), null, message, null));
                    sendMessage(getChannel(), message);
                }
            }
        }, 0, 750, TimeUnit.MILLISECONDS);
    }

    @Override
    public void quit() {
        Datman.setBotStatus(0);
        Datman.setNickStatus(getNick(), "0");
        Datman.quit();
    }

    @Override
    public void receiveEvent(final IrkEvent event, final String login, final String hostName, final String nick, final String target, final String data, final Number number) {
        final Event irk = new Event(event, login, hostName, nick, target, data, number);
        loggingQueue.add(irk);
        react(irk);

        switch (event) {
            case JOIN:
                updateUser(nick, "1");
                break;
            case PART:
                updateUser(nick, "0");
                break;
            default:
                break;
        }
    }

    @Override
    protected void onUserList(final String channel, final User[] users) {
        super.onUserList(channel, users);
        final List<Nick> nicks = new ArrayList<>();
        for (final User user : users) {
            nicks.add(new Nick(user.getNick()));
        }
        Datman.setNickStatus(nicks, "1");
    }

    private void updateUser(final String nick, final String online) {
        Datman.setNickStatus(nick, online);
    }

    @Override
    public void start() {
        scheduleMessageService();
        try {
            setTwitter();
            joinChannel();
        } catch (SQLException | IrcException | IOException e) {
            throw new RuntimeException(e);
        }
        Datman.setBotStatus(1);
    }

    private void addMessage(final Action action, final Reaction reaction) {
        final int    count  = reaction.getCount();
        final String type   = reaction.getReactionType();
        for (int i = 0; i < count; ++i) {
            switch (type) {
                case "Message":
                    final String[] reactions = reaction.getReaction().split("\\|\\|");
                    Collections.addAll(messageQueue, reactions);
                    break;
                case "Tweet":
                    tweet(action, reaction);
                    break;
                default:
            }
        }
    }

    private void joinChannel() throws SQLException, IrcException, IOException {
        final String password = Datman.getIrcPassword(getName());
        connect("irc.twitch.tv", 6667, password);
        joinChannel(getChannel());
    }

    private void react(final Event event) {
        final Action command           = new Action(event);
        final List<Reaction> reactions = Datman.getReactions(command);
        if(reactions.size() > 0) {
            for (final Reaction reaction : reactions) {
                if (reaction.isTriggeredBy(command)) {
                    addMessage(command, reaction);
                }
            }
        }
    }

    private void setTwitter() throws SQLException {
        final Map<String, String> credentials    = Datman.getTwitterCredentials(getName());
        final ConfigurationBuilder configuration = new ConfigurationBuilder()
                .setOAuthAccessToken(credentials.get("AccessToken"))
                .setOAuthAccessTokenSecret(credentials.get("AccessSecret"))
                .setOAuthConsumerKey(credentials.get("ConsumerKey"))
                .setOAuthConsumerSecret(credentials.get("ConsumerSecret"));
        this.twitter = new TwitterFactory(configuration.build()).getInstance();
    }

    private void tweet(final Action action, final Reaction reaction) {
        try {
            twitter.updateStatus(tweetFormat(action.getNick(), action.getAction(), reaction.getReaction()));
        } catch (final TwitterException e) {
            LoggerFactory.getLogger(getClass()).error("Twitter fail", e);
        }
    }

    private String tweetFormat(final String nick, final String message, final String reaction) {
        final int    messageGoal = 140 - (nick.length() + reaction.length());
        final String newMessage  = (message.length() > messageGoal)
                ? message.substring(0, messageGoal - 3).trim() + "..."
                : message;
        return String.format(nick + ": " + reaction, newMessage);
    }

}
