package com.ryancarrigan.peabot;

import com.ryancarrigan.chatman.Nick;

/**
 * Created by Suave Peanut on 2015.1.17.
 */
public class NickService extends ServiceQueue<Nick> {

    private boolean online;

    NickService(final String name, final String channel, final boolean online) {
        super(name, channel);
        this.online = online;
    }

    @Override
    public void run() {
        // Check if there are events waiting to be logged.
        if (this.hasItems()) {
            log.info(String.format("Updating %d users to %sline.", size(), (this.online) ? "on" : "off"));

            // Send all queued events to the database.
            datman().setNickStatus(getQueue(), (this.online) ? "1" : "0");
            // Clear the queue after events have been logged.
            this.clear();
        }
    }

}