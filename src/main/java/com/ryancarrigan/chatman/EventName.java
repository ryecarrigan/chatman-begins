package com.ryancarrigan.chatman;

/**
 * Created by Suave Peanut on 2015.1.6.
 */
public enum EventName {

    ACTION(1),
    CHANNEL_INFO(2),
    CONNECT(3),
    DEOP(4),
    DEVOICE(5),
    DISCONNECT(6),
    INVITE(7),
    JOIN(8),
    KICK(9),
    MESSAGE(10),
    MODE(11),
    NICK_CHANGE(12),
    NOTICE(13),
    OP(14),
    PART(15),
    PRIVATE_MESSAGE(16),
    QUIT(17),
    REMOVE(18),
    REMOVE_CHANNEL_BAN(0),
    REMOVE_CHANNEL_KEY(0),
    SERVER_RESPONSE(19),
    SET(20),
    SET_CHANNEL_BAN(0),
    SET_CHANNEL_KEY(0),
    TOPIC(21),
    UNKNOWN(0),
    USER_LIST(22),
    USER_MODE(23),
    VOICE(24);

    private final int key;

    EventName(final int key) {
        this.key = key;
    }

    int getKey() {
        return this.key;
    }

}
