package com.ryancarrigan.chatman;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Suave Peanut on 2014.12.27.
 */
public class Chatman extends PircBot {

    private final static Logger   log;
    private final static String[] reactables;

    static {
        log        = LoggerFactory.getLogger(Chatman.class);
        reactables = new String[]{"Message"};
    }

    private final DataConnector dataConnector;
    private final String password;

    public Chatman(final String name, final String password, final String channel) {
        this.dataConnector = new DataConnector(channel);
        this.password = password;
        this.setName(name);
        this.setVerbose(true);
    }

    public void connect() throws IrcException, IOException, SQLException {
        this.dataConnector.connect();
        this.connect("irc.twitch.tv", 6667, password);
    }

    public void quit() {
        super.disconnect();
        try {
            this.dataConnector.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void receiveEvent(final String eventName, final String channel, final String login, final String hostname,
                              final String nick, final String target, final String data, final Number number) {
        logEvent(eventName, channel, login, hostname, nick, target, data, number);
        if (Arrays.asList(reactables).contains(eventName)) {
            react(eventName, channel, nick, target, data);
        }
    }

    private void logEvent(final String eventName, final String channel, final String login, final String hostname,
                          final String nick, final String target, final String data, final Number number) {
        final Event event = new Event(eventName, channel, login, hostname, nick, target, data, number);
        try {
            dataConnector.insert(event);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void react(final String eventName, final String channel, final String nick, final String target,
                       final String data) {
        final Reaction command = new Reaction(channel, data, eventName, nick, target, "");
        log.info("ACTION: " + command.toString());
        try {
            final List<Reaction> reactions = dataConnector.react(command);
            final int size = reactions.size();
            if (size > 0) {
                log.info(size + " possible reactions found.");
                for (final Reaction reaction : reactions) {
                    log.info("CHECKING REACTION: " + reaction.toString());
                    if (reaction.hasMatchingMessage(command) && reaction.hasMatchingNick(command)) {
                        switch (reaction.getEventName()) {
                            case "Message":
                                log.info("SENDING MESSAGE: " + command.getAction());
                                sendMessage(reaction.getChannel(), reaction.getReaction());
                                break;
                            default:
                                log.info("NO EVENTTYPE");
                                break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                log.info("No results found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void onConnect() {
        receiveEvent("Connect", null, null, null, null, null, null, null);
    }

    protected void onDisconnect() {
        log.info("DISCONNECT");
    }

    protected void onAction(final String sender, final String login, final String hostname,
                            final String target, final String action) {
        receiveEvent("Action", null, login, hostname, sender, target, action, null);
    }

    protected void onChannelInfo(final String channel, final int userCount, final String topic) {
        receiveEvent("ChannelInfo", channel, null, null, null, null, topic, userCount);
    }

    protected void onDeop(final String channel, final String sourceNick, final String sourceLogin,
                          final String sourceHostname, final String recipient) {
        receiveEvent("Deop", channel, sourceLogin, sourceHostname, sourceNick, recipient, null, null);
    }

    protected void onDeVoice(final String channel, final String sourceNick, final String sourceLogin,
                             final String sourceHostname, final String recipient) {
        receiveEvent("DeVoice", channel, sourceLogin, sourceHostname, sourceNick, recipient, null, null);
    }

    protected void onInvite(final String targetNick, final String sourceNick, final String sourceLogin,
                            final String sourceHostname, final String channel)  {
        receiveEvent("Invite", channel, sourceLogin, sourceHostname, sourceNick, targetNick, null, null);
    }

    protected void onJoin(final String channel, final String sender, final String login, final String hostname) {
        receiveEvent("Join", channel, login, hostname, sender, null, null, null);
    }

    protected void onKick(final String channel, final String kickerNick, final String kickerLogin,
                          final String kickerHostname, final String recipientNick, final String reason) {
        receiveEvent("Kick", channel, kickerLogin, kickerHostname, kickerNick, recipientNick, reason, null);
    }

    protected void onMessage(final String channel, final String sender, final String login, final String hostname,
                             final String message) {
        receiveEvent("Message", channel, login, hostname, sender, null, message, null);
    }

    protected void onMode(final String channel, final String sourceNick, final String sourceLogin,
                          final String sourceHostname, final String mode) {
        receiveEvent("Mode", channel, sourceLogin, sourceHostname, sourceNick, null, mode, null);
    }

    protected void onNickChange(final String oldNick, final String login, final String hostname, final String newNick) {
        receiveEvent("NickChange", null, login, hostname, oldNick, null, newNick, null);
    }

    protected void onNotice(final String sourceNick, final String sourceLogin, final String sourceHostname,
                            final String target, final String notice) {
        receiveEvent("Notice", null, sourceLogin, sourceHostname, sourceNick, target, notice, null);
    }

    protected void onOp(final String channel, final String sourceNick, final String sourceLogin,
                        final String sourceHostname, final String recipient) {
        receiveEvent("Op", channel, sourceLogin, sourceHostname, sourceNick, recipient, null, null);
    }

    protected void onPart(final String channel, final String sender, final String login, final String hostname) {
        receiveEvent("Part", channel, login, hostname, sender, null, null, null);
    }

    protected void onPrivateMessage(final String sender, final String login, final String hostname,
                                    final String message) {
        receiveEvent("PrivateMessage", null, login, hostname, sender, null, message, null);
    }

    protected void onQuit(final String sourceNick, final String sourceLogin, final String sourceHostname,
                          final String reason) {
        receiveEvent("Quit", null, sourceLogin, sourceHostname, sourceNick, null, reason, null);
    }

    protected void onRemoveChannelBan(final String channel, final String sourceNick, final String sourceLogin,
                                      final String sourceHostname, final String hostmask) {
        receiveEvent("RemoveChannelBan", channel, sourceLogin, sourceHostname, sourceNick, null, hostmask, null);
    }

    protected void onRemoveChannelKey(final String channel, final String sourceNick, final String sourceLogin,
                                      final String sourceHostname, final String key) {
        receiveEvent("RemoveChannelKey", channel, sourceLogin, sourceHostname, sourceNick, null, key, null);
    }

    protected void onRemoveChannelLimit(final String channel, final String sourceNick, final String sourceLogin,
                                        final String sourceHostname) {
        receiveEvent("RemoveChannelLimit", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onRemoveInviteOnly(final String channel, final String sourceNick, final String sourceLogin,
                                      final String sourceHostname) {
        receiveEvent("RemoveInviteOnly", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onRemoveModerated(final String channel, final String sourceNick, final String sourceLogin,
                                     final String sourceHostname) {
        receiveEvent("RemoveModerated", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onRemoveNoExternalMessages(final String channel, final String sourceNick, final String sourceLogin,
                                              final String sourceHostname) {
        receiveEvent("RemoveNoExternalMessages", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onRemovePrivate(final String channel, final String sourceNick, final String sourceLogin,
                                   final String sourceHostname) {
        receiveEvent("RemovePrivate", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onRemoveSecret(final String channel, final String sourceNick, final String sourceLogin,
                                  final String sourceHostname) {
        receiveEvent("RemoveSecret", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onRemoveTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
        receiveEvent("RemoveTopicProtection", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onServerResponse(final int code, final String response) {
        receiveEvent("ServerResponse", null, null, null, null, null, response, code);
    }

    protected void onSetChannelBan(final String channel, final String sourceNick, final String sourceLogin,
                                   final String sourceHostname, final String hostmask) {
        receiveEvent("SetChannelBan", channel, sourceLogin, sourceHostname, sourceNick, null, hostmask, null);
    }

    protected void onSetChannelKey(final String channel, final String sourceNick, final String sourceLogin,
                                   final String sourceHostname, final String key) {
        receiveEvent("SetChannelKey", channel, sourceLogin, sourceHostname, sourceNick, null, key, null);
    }

    protected void onSetChannelLimit(final String channel, final String sourceNick, final String sourceLogin,
                                     final String sourceHostname, final int limit) {
        receiveEvent("SetChannelLimit", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onSetInviteOnly(final String channel, final String sourceNick, final String sourceLogin,
                                   final String sourceHostname) {
        receiveEvent("SetInviteOnly", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onSetModerated(final String channel, final String sourceNick, final String sourceLogin,
                                  final String sourceHostname) {
        receiveEvent("SetModerated", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onSetNoExternalMessages(final String channel, final String sourceNick, final String sourceLogin,
                                           final String sourceHostname) {
        receiveEvent("SetNoExternalMessages", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onSetPrivate(final String channel, final String sourceNick, final String sourceLogin,
                                final String sourceHostname) {
        receiveEvent("SetPrivate", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onSetSecret(final String channel, final String sourceNick, final String sourceLogin,
                               final String sourceHostname) {
        receiveEvent("SetSecret", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onSetTopicProtection(final String channel, final String sourceNick, final String sourceLogin,
                                        final String sourceHostname) {
        receiveEvent("SetTopicProtection", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onTopic(final String channel, final String topic, final String setBy, final long date,
                           final boolean changed) {
        receiveEvent("Topic", channel, null, null, setBy, null, Boolean.toString(changed), date);
    }

    protected void onUnknown(final String line) {
        receiveEvent("Unknown", null, null, null, null, null, line, null);
    }

    protected void onUserList(final String channel, final User[] users) {
        receiveEvent("Unknown", channel, null, null, null, null, Arrays.toString(users), null);
    }

    protected void onUserMode(final String targetNick, final String sourceNick, final String sourceLogin,
                              final String sourceHostname, final String mode) {
        receiveEvent("UserMode", null, sourceLogin, sourceHostname, sourceNick, targetNick, mode, null);
    }

    protected void onVoice(final String channel, final String sourceNick, final String sourceLogin,
                           final String sourceHostname, final String recipient) {
        receiveEvent("Voice", channel, sourceLogin, sourceHostname, sourceNick, recipient, null, null);
    }

}
