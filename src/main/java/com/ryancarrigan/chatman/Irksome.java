package com.ryancarrigan.chatman;

import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import java.util.Arrays;

/**
 * Created by Suave Peanut on 2015.1.6.
 */
public abstract class Irksome extends PircBot {

    private String channel;

    public Irksome(final String channel, final String login) {
        this.channel = "#" + channel;
        this.setName(login);
    }

    public String getChannel() {
        return this.channel;
    }

    protected void onAction(final String sender, final String login, final String hostname, final String target,
                            final String action) {
        receiveEvent(IrkEvent.ACTION, login, hostname, sender, target, action, null);
    }

    protected void onChannelInfo(final String channel, final int userCount, final String topic) {
        receiveEvent(IrkEvent.CHANNEL_INFO, null, null, null, null, topic, userCount);
    }

    protected void onConnect() {
        receiveEvent(IrkEvent.CONNECT, null, null, null, null, null, null);
    }

    protected void onDeop(final String channel, final String nick, final String login, final String hostName,
                          final String recipient) {
        receiveEvent(IrkEvent.DEOP, login, hostName, nick, recipient, null, null);
    }

    protected void onDeVoice(final String channel, final String nick, final String login, final String hostName,
                             final String recipient) {
        receiveEvent(IrkEvent.DEVOICE, login, hostName, nick, recipient, null, null);
    }

    protected void onDisconnect() {
        receiveEvent(IrkEvent.DISCONNECT, null, null, null, null, null, null);
    }

    protected void onInvite(final String target, final String nick, final String login, final String hostName,
                            final String channel)  {
        receiveEvent(IrkEvent.INVITE, login, hostName, nick, target, null, null);
    }

    protected void onJoin(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent(IrkEvent.JOIN, login, hostName, nick, null, null, null);
    }

    protected void onKick(final String channel, final String nick, final String login, final String hostName,
                          final String target, final String reason) {
        receiveEvent(IrkEvent.KICK, login, hostName, nick, target, reason, null);
    }

    protected void onMessage(final String channel, final String nick, final String login, final String hostName,
                             final String message) {
        receiveEvent(IrkEvent.MESSAGE, login, hostName, nick, null, message, null);
    }

    protected void onMode(final String channel, final String nick, final String login, final String hostName,
                          final String mode) {
        receiveEvent(IrkEvent.MODE, login, hostName, nick, null, mode, null);
    }

    protected void onNickChange(final String nick, final String login, final String hostName, final String newNick) {
        receiveEvent(IrkEvent.NICK_CHANGE, login, hostName, nick, null, newNick, null);
    }

    protected void onNotice(final String nick, final String login, final String hostName, final String target,
                            final String notice) {
        receiveEvent(IrkEvent.NOTICE, login, hostName, nick, target, notice, null);
    }

    protected void onOp(final String channel, final String nick, final String login, final String hostName,
                        final String recipient) {
        receiveEvent(IrkEvent.OP, login, hostName, nick, recipient, null, null);
    }

    protected void onPart(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent(IrkEvent.PART, login, hostName, nick, null, null, null);
    }

    protected void onPrivateMessage(final String nick, final String login, final String hostName,
                                    final String message) {
        receiveEvent(IrkEvent.PRIVATE_MESSAGE, login, hostName, nick, null, message, null);
    }

    protected void onQuit(final String nick, final String login, final String hostName, final String reason) {
        receiveEvent(IrkEvent.QUIT, login, hostName, nick, null, reason, null);
    }

    protected void onRemoveChannelBan(final String channel, final String nick, final String login,
                                      final String hostName, final String hostMask) {
        receiveEvent(IrkEvent.REMOVE_CHANNEL_BAN, login, hostName, nick, null, hostMask, null);
    }

    protected void onRemoveChannelKey(final String channel, final String nick, final String login,
                                      final String hostName, final String channelKey) {
        receiveEvent(IrkEvent.REMOVE_CHANNEL_KEY, login, hostName, nick, null, channelKey, null);
    }

    protected void onRemoveChannelLimit(final String channel, final String nick, final String login,
                                        final String hostName) {
        receiveEvent(IrkEvent.REMOVE, login, hostName, nick, null, "ChannelLimit", null);
    }

    protected void onRemoveInviteOnly(final String channel, final String nick, final String login,
                                      final String hostName) {
        receiveEvent(IrkEvent.REMOVE, login, hostName, nick, null, "InviteOnly", null);
    }

    protected void onRemoveModerated(final String channel, final String nick, final String login,
                                     final String hostName) {
        receiveEvent(IrkEvent.REMOVE, login, hostName, nick, null, "Moderated", null);
    }

    protected void onRemoveNoExternalMessages(final String channel, final String nick, final String login,
                                              final String hostName) {
        receiveEvent(IrkEvent.REMOVE, login, hostName, nick, null, "NoExternalMessages", null);
    }

    protected void onRemovePrivate(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent(IrkEvent.REMOVE, login, hostName, nick, null, "Private", null);
    }

    protected void onRemoveSecret(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent(IrkEvent.REMOVE, login, hostName, nick, null, "Secret", null);
    }

    protected void onRemoveTopicProtection(final String channel, final String nick, final String login,
                                           final String hostName) {
        receiveEvent(IrkEvent.REMOVE, login, hostName, nick, null, "TopicProtection", null);
    }

    protected void onServerResponse(final int code, final String response) {
        receiveEvent(IrkEvent.SERVER_RESPONSE, null, null, null, null, response, code);
    }

    protected void onSetChannelBan(final String channel, final String nick, final String login, final String hostName,
                                   final String hostMask) {
        receiveEvent(IrkEvent.SET_CHANNEL_BAN, login, hostName, nick, null, hostMask, null);
    }

    protected void onSetChannelKey(final String channel, final String nick, final String login, final String hostName,
                                   final String channelKey) {
        receiveEvent(IrkEvent.SET_CHANNEL_KEY, login, hostName, nick, null, channelKey, null);
    }

    protected void onSetChannelLimit(final String channel, final String nick, final String login, final String hostName,
                                     final int limit) {
        receiveEvent(IrkEvent.SET, login, hostName, nick, null, "ChannelLimit", limit);
    }

    protected void onSetInviteOnly(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent(IrkEvent.SET, login, hostName, nick, null, "InviteOnly", null);
    }

    protected void onSetModerated(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent(IrkEvent.SET, login, hostName, nick, null, "Moderated", null);
    }

    protected void onSetNoExternalMessages(final String channel, final String nick, final String login,
                                           final String hostName) {
        receiveEvent(IrkEvent.SET, login, hostName, nick, null, "NoExternalMessages", null);
    }

    protected void onSetPrivate(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent(IrkEvent.SET, login, hostName, nick, null, "Private", null);
    }

    protected void onSetSecret(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent(IrkEvent.SET, login, hostName, nick, null, "Secret", null);
    }

    protected void onSetTopicProtection(final String channel, final String nick, final String login,
                                        final String hostName) {
        receiveEvent(IrkEvent.SET, login, hostName, nick, null, "TopicProtection", null);
    }

    protected void onTopic(final String channel, final String topic, final String nick, final long date,
                           final boolean data) {
        receiveEvent(IrkEvent.TOPIC, null, null, nick, null, Boolean.toString(data), date);
    }

    protected void onUnknown(final String data) {
        receiveEvent(IrkEvent.UNKOWN, null, null, null, null, data, null);
    }

    protected void onUserList(final String channel, final User[] users) {
        receiveEvent(IrkEvent.USER_LIST, null, null, null, null, Arrays.toString(users), null);
    }

    protected void onUserMode(final String target, final String nick, final String login, final String hostName,
                              final String mode) {
        receiveEvent(IrkEvent.USER_MODE, login, hostName, nick, target, mode, null);
    }

    protected void onVoice(final String channel, final String nick, final String login, final String hostName,
                           final String recipient) {
        receiveEvent(IrkEvent.VOICE, login, hostName, nick, recipient, null, null);
    }

    public abstract void quit();

    protected abstract void receiveEvent(final IrkEvent event, final String sourceLogin, final String hostName,
                                         final String nick, final String target, final String data, final Number number);

    public abstract void start();

}
