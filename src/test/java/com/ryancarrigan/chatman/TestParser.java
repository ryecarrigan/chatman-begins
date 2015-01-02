package com.ryancarrigan.chatman;

import org.testng.annotations.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Suave Peanut on 2015.1.1.
 */
public class TestParser {

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

}
