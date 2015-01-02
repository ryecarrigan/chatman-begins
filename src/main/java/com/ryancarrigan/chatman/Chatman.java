package com.ryancarrigan.chatman;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Created by Suave Peanut on 2014.12.27.
 */
public class Chatman extends PircBot {

    private final static Logger log;

    static {
        log = LoggerFactory.getLogger(Chatman.class);
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

    private void logEvent(final String eventName, final String channel, final String login, final String hostname,
                          final String nick, final String target, final String data, final Number number) {
        final Event event = new Event(eventName, channel, login, hostname, nick, target, data, number);
        final Reaction reaction = new Reaction(channel, data, eventName, nick, target);
        try {
            dataConnector.insert(event);
            dataConnector.react(reaction);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void onConnect() {
        logEvent("Connect", null, null, null, null, null, null, null);
    }

    protected void onDisconnect() {
        log.info("DISCONNECT");
    }

    protected void onAction(final String sender, final String login, final String hostname,
                            final String target, final String action) {
        logEvent("Action", null, login, hostname, sender, target, action, null);
    }

    protected void onChannelInfo(final String channel, final int userCount, final String topic) {
        logEvent("ChannelInfo", channel, null, null, null, null, topic, userCount);
    }

    protected void onDeop(final String channel, final String sourceNick, final String sourceLogin,
                          final String sourceHostname, final String recipient) {
        logEvent("Deop", channel, sourceLogin, sourceHostname, sourceNick, recipient, null, null);
    }

    protected void onDeVoice(final String channel, final String sourceNick, final String sourceLogin,
                             final String sourceHostname, final String recipient) {
        logEvent("DeVoice", channel, sourceLogin, sourceHostname, sourceNick, recipient, null, null);
    }

    protected void onInvite(final String targetNick, final String sourceNick, final String sourceLogin,
                            final String sourceHostname, final String channel)  {
        logEvent("Invite", channel, sourceLogin, sourceHostname, sourceNick, targetNick, null, null);
    }

    protected void onJoin(final String channel, final String sender, final String login, final String hostname) {
        logEvent("Join", channel, login, hostname, sender, null, null, null);
    }

    protected void onKick(final String channel, final String kickerNick, final String kickerLogin,
                          final String kickerHostname, final String recipientNick, final String reason) {
        logEvent("Kick", channel, kickerLogin, kickerHostname, kickerNick, recipientNick, reason, null);
    }

    protected void onMessage(final String channel, final String sender, final String login, final String hostname,
                             final String message) {
        logEvent("Message", channel, login, hostname, sender, null, message, null);
    }

    protected void onMode(final String channel, final String sourceNick, final String sourceLogin,
                          final String sourceHostname, final String mode) {
        logEvent("Mode", channel, sourceLogin, sourceHostname, sourceNick, null, mode, null);
    }

    protected void onNickChange(final String oldNick, final String login, final String hostname, final String newNick) {
        logEvent("NickChange", null, login, hostname, oldNick, null, newNick, null);
    }

    protected void onNotice(final String sourceNick, final String sourceLogin, final String sourceHostname,
                            final String target, final String notice) {
        logEvent("Notice", null, sourceLogin, sourceHostname, sourceNick, target, notice, null);
    }

    protected void onOp(final String channel, final String sourceNick, final String sourceLogin,
                        final String sourceHostname, final String recipient) {
        logEvent("Op", channel, sourceLogin, sourceHostname, sourceNick, recipient, null, null);
    }

    protected void onPart(final String channel, final String sender, final String login, final String hostname) {
        logEvent("Part", channel, login, hostname, sender, null, null, null);
    }

    protected void onPrivateMessage(final String sender, final String login, final String hostname,
                                    final String message) {
        logEvent("PrivateMessage", null, login, hostname, sender, null, message, null);
    }

    protected void onQuit(final String sourceNick, final String sourceLogin, final String sourceHostname,
                          final String reason) {
        logEvent("Quit", null, sourceLogin, sourceHostname, sourceNick, null, reason, null);
    }

    protected void onRemoveChannelBan(final String channel, final String sourceNick, final String sourceLogin,
                                      final String sourceHostname, final String hostmask) {
        logEvent("RemoveChannelBan", channel, sourceLogin, sourceHostname, sourceNick, null, hostmask, null);
    }

    protected void onRemoveChannelKey(final String channel, final String sourceNick, final String sourceLogin,
                                      final String sourceHostname, final String key) {
        logEvent("RemoveChannelKey", channel, sourceLogin, sourceHostname, sourceNick, null, key, null);
    }

    protected void onRemoveChannelLimit(final String channel, final String sourceNick, final String sourceLogin,
                                        final String sourceHostname) {
        logEvent("RemoveChannelLimit", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onRemoveInviteOnly(final String channel, final String sourceNick, final String sourceLogin,
                                      final String sourceHostname) {
        logEvent("RemoveInviteOnly", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onRemoveModerated(final String channel, final String sourceNick, final String sourceLogin,
                                     final String sourceHostname) {
        logEvent("RemoveModerated", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onRemoveNoExternalMessages(final String channel, final String sourceNick, final String sourceLogin,
                                              final String sourceHostname) {
        logEvent("RemoveNoExternalMessages", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onRemovePrivate(final String channel, final String sourceNick, final String sourceLogin,
                                   final String sourceHostname) {
        logEvent("RemovePrivate", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onRemoveSecret(final String channel, final String sourceNick, final String sourceLogin,
                                  final String sourceHostname) {
        logEvent("RemoveSecret", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onRemoveTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
        logEvent("RemoveTopicProtection", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onServerResponse(final int code, final String response) {
        logEvent("ServerResponse", null, null, null, null, null, response, code);
    }

    protected void onSetChannelBan(final String channel, final String sourceNick, final String sourceLogin,
                                   final String sourceHostname, final String hostmask) {
        logEvent("SetChannelBan", channel, sourceLogin, sourceHostname, sourceNick, null, hostmask, null);
    }

    protected void onSetChannelKey(final String channel, final String sourceNick, final String sourceLogin,
                                   final String sourceHostname, final String key) {
        logEvent("SetChannelKey", channel, sourceLogin, sourceHostname, sourceNick, null, key, null);
    }

    protected void onSetChannelLimit(final String channel, final String sourceNick, final String sourceLogin,
                                     final String sourceHostname, final int limit) {
        logEvent("SetChannelLimit", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onSetInviteOnly(final String channel, final String sourceNick, final String sourceLogin,
                                   final String sourceHostname) {
        logEvent("SetInviteOnly", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onSetModerated(final String channel, final String sourceNick, final String sourceLogin,
                                  final String sourceHostname) {
        logEvent("SetModerated", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onSetNoExternalMessages(final String channel, final String sourceNick, final String sourceLogin,
                                           final String sourceHostname) {
        logEvent("SetNoExternalMessages", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onSetPrivate(final String channel, final String sourceNick, final String sourceLogin,
                                final String sourceHostname) {
        logEvent("SetPrivate", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onSetSecret(final String channel, final String sourceNick, final String sourceLogin,
                               final String sourceHostname) {
        logEvent("SetSecret", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onSetTopicProtection(final String channel, final String sourceNick, final String sourceLogin,
                                        final String sourceHostname) {
        logEvent("SetTopicProtection", channel, sourceLogin, sourceHostname, sourceNick, null, null, null);
    }

    protected void onTopic(final String channel, final String topic, final String setBy, final long date,
                           final boolean changed) {
        logEvent("Topic", channel, null, null, setBy, null, Boolean.toString(changed), date);
    }

    protected void onUnknown(final String line) {
        logEvent("Unknown", null, null, null, null, null, line, null);
    }

    protected void onUserList(final String channel, final User[] users) {
        logEvent("Unknown", channel, null, null, null, null, Arrays.toString(users), null);
    }

    protected void onUserMode(final String targetNick, final String sourceNick, final String sourceLogin,
                              final String sourceHostname, final String mode) {
        logEvent("UserMode", null, sourceLogin, sourceHostname, sourceNick, targetNick, mode, null);
    }

    protected void onVoice(final String channel, final String sourceNick, final String sourceLogin,
                           final String sourceHostname, final String recipient) {
        logEvent("Voice", channel, sourceLogin, sourceHostname, sourceNick, recipient, null, null);
    }

}
