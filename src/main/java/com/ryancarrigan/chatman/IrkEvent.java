package com.ryancarrigan.chatman;

/**
 * Created by Suave Peanut on 2015.1.6.
 */
public enum IrkEvent {

    ACTION("Action"),
    CHANNEL_INFO("ChannelInfo"),
    CONNECT("Connect"),
    DEOP("Deop"),
    DEVOICE("DeVoice"),
    DISCONNECT("Disconnect"),
    INVITE("Invite"),
    JOIN("Join"),
    KICK("Kick"),
    MESSAGE("Message"),
    MODE("Mode"),
    NICK_CHANGE("NickChange"),
    NOTICE("Notice"),
    OP("Op"),
    PART("Part"),
    PRIVATE_MESSAGE("PrivateMessage"),
    QUIT("Quit"),
    REMOVE("Remove"),
    REMOVE_CHANNEL_BAN("RemoveChannelBan"),
    REMOVE_CHANNEL_KEY("RemoveChannelKey"),
    SERVER_RESPONSE("ServerResponse"),
    SET("Set"),
    SET_CHANNEL_BAN("SetChannelBan"),
    SET_CHANNEL_KEY("SetChannelKey"),
    TOPIC("Topic"),
    UNKOWN("Unknown"),
    USER_LIST("UserList"),
    USER_MODE("UserMode"),
    VOICE("Voice");

    private final String name;

    IrkEvent(final String name) {
        this.name = name;
    }

    String getName() {
        return this.name;
    }

}
