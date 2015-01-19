package com.ryancarrigan.chatman;

import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import java.util.Arrays;

/**
 * Created by Suave Peanut on 2015.1.6.
 */
public abstract class IrcBot extends PircBot {

    private String channel;

    public IrcBot(final String channel, final String login) {
        this.channel = "#" + channel;
        this.setName(login);
    }

    public String getChannel() {
        return this.channel;
    }

    protected void onAction(final String nick, final String login, final String hostname, final String target,
                            final String action) {
        receiveEvent(EventName.ACTION, nick, action);
    }

    protected void onChannelInfo(final String channel, final int userCount, final String topic) {
        receiveEvent(EventName.CHANNEL_INFO, Integer.toString(userCount), topic);
    }

    protected void onConnect() {
        receiveEvent(EventName.CONNECT, null, null);
    }

    protected void onDeop(final String channel, final String nick, final String login, final String hostName,
                          final String recipient) {
        receiveEvent(EventName.DEOP, nick, recipient);
    }

    protected void onDeVoice(final String channel, final String nick, final String login, final String hostName,
                             final String recipient) {
        receiveEvent(EventName.DEVOICE, nick, recipient);
    }

    protected void onDisconnect() {
        receiveEvent(EventName.DISCONNECT, null, null);
    }

    protected void onInvite(final String target, final String nick, final String login, final String hostName,
                            final String channel)  {
        receiveEvent(EventName.INVITE, nick, null);
    }

    protected void onJoin(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent(EventName.JOIN, nick, null);
    }

    protected void onKick(final String channel, final String nick, final String login, final String hostName,
                          final String target, final String reason) {
        receiveEvent(EventName.KICK, nick, reason);
    }

    protected void onMessage(final String channel, final String nick, final String login, final String hostName,
                             final String message) {
        receiveEvent(EventName.MESSAGE, nick, message);
    }

    protected void onMode(final String channel, final String nick, final String login, final String hostName,
                          final String mode) {
        receiveEvent(EventName.MODE, nick, mode);
    }

    protected void onNickChange(final String nick, final String login, final String hostName, final String newNick) {
        receiveEvent(EventName.NICK_CHANGE, nick, newNick);
    }

    protected void onNotice(final String nick, final String login, final String hostName, final String target,
                            final String notice) {
        receiveEvent(EventName.NOTICE, nick, notice);
    }

    protected void onOp(final String channel, final String nick, final String login, final String hostName,
                        final String recipient) {
        receiveEvent(EventName.OP, nick, recipient);
    }

    protected void onPart(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent(EventName.PART, nick, null);
    }

    protected void onPrivateMessage(final String nick, final String login, final String hostName,
                                    final String message) {
        receiveEvent(EventName.PRIVATE_MESSAGE, nick, message);
    }

    protected void onQuit(final String nick, final String login, final String hostName, final String reason) {
        receiveEvent(EventName.QUIT, nick, reason);
    }

    protected void onRemoveChannelBan(final String channel, final String nick, final String login,
                                      final String hostName, final String hostMask) {
        receiveEvent(EventName.REMOVE_CHANNEL_BAN, nick, hostMask);
    }

    protected void onRemoveChannelKey(final String channel, final String nick, final String login,
                                      final String hostName, final String channelKey) {
        receiveEvent(EventName.REMOVE_CHANNEL_KEY, nick, channelKey);
    }

    protected void onRemoveChannelLimit(final String channel, final String nick, final String login,
                                        final String hostName) {
        receiveEvent(EventName.REMOVE, nick, "ChannelLimit");
    }

    protected void onRemoveInviteOnly(final String channel, final String nick, final String login,
                                      final String hostName) {
        receiveEvent(EventName.REMOVE, nick, "InviteOnly");
    }

    protected void onRemoveModerated(final String channel, final String nick, final String login,
                                     final String hostName) {
        receiveEvent(EventName.REMOVE, nick, "Moderated");
    }

    protected void onRemoveNoExternalMessages(final String channel, final String nick, final String login,
                                              final String hostName) {
        receiveEvent(EventName.REMOVE, nick, "NoExternalMessages");
    }

    protected void onRemovePrivate(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent(EventName.REMOVE, nick, "Private");
    }

    protected void onRemoveSecret(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent(EventName.REMOVE, nick, "Secret");
    }

    protected void onRemoveTopicProtection(final String channel, final String nick, final String login,
                                           final String hostName) {
        receiveEvent(EventName.REMOVE, nick, "TopicProtection");
    }

    protected void onServerResponse(final int code, final String response) {
        receiveEvent(EventName.SERVER_RESPONSE, Integer.toString(code), response);
    }

    protected void onSetChannelBan(final String channel, final String nick, final String login, final String hostName,
                                   final String hostMask) {
        receiveEvent(EventName.SET_CHANNEL_BAN, nick, hostMask);
    }

    protected void onSetChannelKey(final String channel, final String nick, final String login, final String hostName,
                                   final String channelKey) {
        receiveEvent(EventName.SET_CHANNEL_KEY, nick, channelKey);
    }

    protected void onSetChannelLimit(final String channel, final String nick, final String login, final String hostName,
                                     final int limit) {
        receiveEvent(EventName.SET, nick, String.format("ChannelLimit: %d", limit));
    }

    protected void onSetInviteOnly(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent(EventName.SET, nick, "InviteOnly");
    }

    protected void onSetModerated(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent(EventName.SET, nick, "Moderated");
    }

    protected void onSetNoExternalMessages(final String channel, final String nick, final String login,
                                           final String hostName) {
        receiveEvent(EventName.SET, nick, "NoExternalMessages");
    }

    protected void onSetPrivate(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent(EventName.SET, nick, "Private");
    }

    protected void onSetSecret(final String channel, final String nick, final String login, final String hostName) {
        receiveEvent(EventName.SET, nick, "Secret");
    }

    protected void onSetTopicProtection(final String channel, final String nick, final String login,
                                        final String hostName) {
        receiveEvent(EventName.SET, nick, "TopicProtection");
    }

    protected void onTopic(final String channel, final String topic, final String nick, final long date,
                           final boolean data) {
        receiveEvent(EventName.TOPIC, nick, Boolean.toString(data));
    }

    protected void onUnknown(final String data) {
        receiveEvent(EventName.UNKNOWN, null, data);
    }

    protected void onUserList(final String channel, final User[] users) {
        receiveEvent(EventName.USER_LIST, null, Arrays.toString(users));
    }

    protected void onUserMode(final String target, final String nick, final String login, final String hostName,
                              final String mode) {
        receiveEvent(EventName.USER_MODE, nick, mode);
    }

    protected void onVoice(final String channel, final String nick, final String login, final String hostName,
                           final String target) {
        receiveEvent(EventName.VOICE, nick, null);
    }

    public abstract void quit();

    protected abstract void receiveEvent(final EventName event, final String nick, final String data);

    public abstract void start();

}
