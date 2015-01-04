package com.ryancarrigan.chatman;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Suave Peanut on 2015.1.1.
 */
public class TestParser {

    private final static Logger log = LoggerFactory.getLogger(TestParser.class);

    @Test
    public void parse() {
        final String logLine = "INFO  Event - <'Part'> ('#riptidepow') - 'fakie_whattt' : 'fakie_whattt.tmi.twitch.tv' : 'fakie_whattt' - NULL { NULL } [NULL]";

        final Pattern pattern = Pattern.compile("^INFO\\s+Event\\s-\\s<'?([^']+)'?>\\s\\('?([^']+)'?\\)\\s-\\s'?([^']+)'?\\s:\\s'?([^']+)'?\\s:\\s'?([^']+)'?\\s-\\s([^']+)'?\\s\\{\\s'?([^']+)'?\\s\\}\\s\\[([^']+)\\]");
        final Matcher matcher = pattern.matcher(logLine);

        if (matcher.find()) {
            final int count = matcher.groupCount();
            if (count == 8) {
                for (int i = 0; i < 8; ++i) System.out.println(matcher.group(i));
            }
        }
    }

    @Test
    public void testEncode(){
        final String s ="└( ͡° ︿ °͡ )┘";
        System.out.println(s);

        final String t;
        try {
            t = new String(s.getBytes("UTF-16"));
            System.out.println(t);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLog() {
        getLogMessage(null, "Join", null, null);
        getLogMessage("#suavepeanut", "Message", "suavepeanut", "Hi!");
    }

    void getLogMessage(String channel, String eventName, String nick, String data) {
         log.info(String.format("%s%s%s%s",
                 (channel == null) ? "" : "[" + channel + "] ",
                 eventName,
                 (nick == null) ? "" : " - " + nick,
                 (data == null) ? "" : ": " + data));
    }

}
