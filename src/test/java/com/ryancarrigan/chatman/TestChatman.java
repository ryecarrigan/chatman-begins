package com.ryancarrigan.chatman;

import org.jibble.pircbot.IrcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.fail;

/**
 * Created by Suave Peanut on 2014.12.27.
 */
public class TestChatman {

    private final static Logger log     = LoggerFactory.getLogger(TestChatman.class);
    private final static String channel = "#riptidepow";

    private Chatman getChatman() {
        return new Chatman("SuavePeabot", "oauth:b5ygqi63qb6fb92gacu57lb1fblw3j", "riptidepow");
    }

    @Test
    public void newChatman() throws IOException, IrcException {
        final String statement = new Event("EventName", null, null, null, null, null, null, null).getStatement("EventLog_sample");
        log.info(statement);
    }

    @Test
    public void channelName() {
        try {
            DataConnector dataConnector = new DataConnector("riptidepow");
            fail();
        } catch (IllegalArgumentException ignored) {

        }
    }

}
