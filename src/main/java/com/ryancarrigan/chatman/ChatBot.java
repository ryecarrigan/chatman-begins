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
 * Created by Suave Peanut on 2015.1.2.
 */
abstract class ChatBot extends PircBot {

    private final static Logger log = LoggerFactory.getLogger(ChatBot.class);

    private   DataConnector dataConnector;
    protected String        login;
    protected String        channel;

    ChatBot(final String channel, final String login, final String databaseName, final String databaseLogin,
            final String databasePassword) {
        this.channel       = "#" + channel;
        this.dataConnector = new DataConnector(databaseName, databaseLogin, databasePassword, channel);
        this.login         = login;
        this.setName(login);
    }

    protected void connectToChannel() {
        try {
            this.dataConnector.connect();
            this.connect("irc.twitch.tv", 6667, this.dataConnector.getPassword(login));
        } catch (final SQLException sqle) {
            log.error(sqle.getMessage());
            throw new IllegalStateException("Database could not be reached.");
        } catch (IOException | IrcException e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException("Error");
        }
    }

    protected DataConnector getDataConnector() {
        return this.dataConnector;
    }

    public void quit() {
        super.disconnect();
        try {
            this.dataConnector.disconnect();
        } catch (final SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    protected void onConnect() {
        receiveEvent("Connect", null, null, null, null, null, null, null);
        joinChannel(channel);
    }

    protected void onDisconnect() {
        receiveEvent("Disconnect", null, null, null, null, null, null, null);
    }

    protected void onAction(final String sender, final String login, final String hostname, final String target,
                            final String action) {
        receiveEvent("Action", null, login, hostname, sender, target, action, null);
    }

    protected void onChannelInfo(final String channel, final int userCount, final String topic) {
        receiveEvent("ChannelInfo", channel, null, null, null, null, topic, userCount);
    }

    protected void onDeop(final String channel, final String nick, final String login, final String hostName,
                          final String recipient) {
        receiveEvent("Deop", channel, login, hostName, nick, recipient, null, null);
    }

    protected void onDeVoice(final String channel, final String nick, final String login, final String hostName,
                             final String recipient) {
        receiveEvent("DeVoice", channel, login, hostName, nick, recipient, null, null);
    }

    protected void onInvite(final String target, final String nick, final String login, final String hostName,
                            final String channel)  {
        receiveEvent("Invite", channel, login, hostName, nick, target, null, null);
    }

    protected void onJoin(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent("Join", channel, login, hostName, nick, null, null, null);
    }

    protected void onKick(final String channel, final String nick, final String login, final String hostName,
                          final String target, final String reason) {
        receiveEvent("Kick", channel, login, hostName, nick, target, reason, null);
    }

    protected void onMessage(final String channel, final String nick, final String login, final String hostName,
                             final String message) {
        receiveEvent("Message", channel, login, hostName, nick, null, message, null);
    }

    protected void onMode(final String channel, final String nick, final String login, final String hostName,
                          final String mode) {
        receiveEvent("Mode", channel, login, hostName, nick, null, mode, null);
    }

    protected void onNickChange(final String nick, final String login, final String hostName, final String newNick) {
        receiveEvent("NickChange", null, login, hostName, nick, null, newNick, null);
    }

    protected void onNotice(final String nick, final String login, final String hostName, final String target,
                            final String notice) {
        receiveEvent("Notice", null, login, hostName, nick, target, notice, null);
    }

    protected void onOp(final String channel, final String nick, final String login, final String hostName,
                        final String recipient) {
        receiveEvent("Op", channel, login, hostName, nick, recipient, null, null);
    }

    protected void onPart(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent("Part", channel, login, hostName, nick, null, null, null);
    }

    protected void onPrivateMessage(final String nick, final String login, final String hostName,
                                    final String message) {
        receiveEvent("PrivateMessage", null, login, hostName, nick, null, message, null);
    }

    protected void onQuit(final String nick, final String login, final String hostName, final String reason) {
        receiveEvent("Quit", null, login, hostName, nick, null, reason, null);
    }

    protected void onRemoveChannelBan(final String channel, final String nick, final String login,
                                      final String hostName, final String hostMask) {
        receiveEvent("RemoveChannelBan", channel, login, hostName, nick, null, hostMask, null);
    }

    protected void onRemoveChannelKey(final String channel, final String nick, final String login,
                                      final String hostName, final String channelKey) {
        receiveEvent("RemoveChannelKey", channel, login, hostName, nick, null, channelKey, null);
    }

    protected void onRemoveChannelLimit(final String channel, final String nick, final String login,
                                        final String hostName) {
        receiveEvent("RemoveChannelLimit", channel, login, hostName, nick, null, null, null);
    }

    protected void onRemoveInviteOnly(final String channel, final String nick, final String login,
                                      final String hostName) {
        receiveEvent("RemoveInviteOnly", channel, login, hostName, nick, null, null, null);
    }

    protected void onRemoveModerated(final String channel, final String nick, final String login,
                                     final String hostName) {
        receiveEvent("RemoveModerated", channel, login, hostName, nick, null, null, null);
    }

    protected void onRemoveNoExternalMessages(final String channel, final String nick, final String login,
                                              final String hostName) {
        receiveEvent("RemoveNoExternalMessages", channel, login, hostName, nick, null, null, null);
    }

    protected void onRemovePrivate(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent("RemovePrivate", channel, login, hostName, nick, null, null, null);
    }

    protected void onRemoveSecret(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent("RemoveSecret", channel, login, hostName, nick, null, null, null);
    }

    protected void onRemoveTopicProtection(final String channel, final String nick, final String login,
                                           final String hostName) {
        receiveEvent("RemoveTopicProtection", channel, login, hostName, nick, null, null, null);
    }

    protected void onServerResponse(final int code, final String response) {
        receiveEvent("ServerResponse", null, null, null, null, null, response, code);
    }

    protected void onSetChannelBan(final String channel, final String nick, final String login, final String hostName,
                                   final String hostMask) {
        receiveEvent("SetChannelBan", channel, login, hostName, nick, null, hostMask, null);
    }

    protected void onSetChannelKey(final String channel, final String nick, final String login, final String hostName,
                                   final String channelKey) {
        receiveEvent("SetChannelKey", channel, login, hostName, nick, null, channelKey, null);
    }

    protected void onSetChannelLimit(final String channel, final String nick, final String login, final String hostName,
                                     final int limit) {
        receiveEvent("SetChannelLimit", channel, login, hostName, nick, null, Integer.toString(limit), null);
    }

    protected void onSetInviteOnly(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent("SetInviteOnly", channel, login, hostName, nick, null, null, null);
    }

    protected void onSetModerated(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent("SetModerated", channel, login, hostName, nick, null, null, null);
    }

    protected void onSetNoExternalMessages(final String channel, final String nick, final String login,
                                           final String hostName) {
        receiveEvent("SetNoExternalMessages", channel, login, hostName, nick, null, null, null);
    }

    protected void onSetPrivate(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent("SetPrivate", channel, login, hostName, nick, null, null, null);
    }

    protected void onSetSecret(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent("SetSecret", channel, login, hostName, nick, null, null, null);
    }

    protected void onSetTopicProtection(final String channel, final String nick, final String login,
                                        final String hostName) {
        receiveEvent("SetTopicProtection", channel, login, hostName, nick, null, null, null);
    }

    protected void onTopic(final String channel, final String topic, final String nick, final long date,
                           final boolean data) {
        receiveEvent("Topic", channel, null, null, nick, null, Boolean.toString(data), date);
    }

    protected void onUnknown(final String data) {
        receiveEvent("Unknown", null, null, null, null, null, data, null);
    }

    protected void onUserList(final String channel, final User[] users) {
        receiveEvent("Unknown", channel, null, null, null, null, Arrays.toString(users), null);
    }

    protected void onUserMode(final String target, final String nick, final String login, final String hostName,
                              final String mode) {
        receiveEvent("UserMode", null, login, hostName, nick, target, mode, null);
    }

    protected void onVoice(final String channel, final String nick, final String login, final String hostName,
                           final String recipient) {
        receiveEvent("Voice", channel, login, hostName, nick, recipient, null, null);
    }

    abstract void receiveEvent(final String eventName, final String channel, final String sourceLogin,
                               final String hostName, final String nick, final String target, final String data,
                               final Number number);

}
