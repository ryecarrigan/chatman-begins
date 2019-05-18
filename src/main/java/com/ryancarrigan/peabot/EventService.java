package com.ryancarrigan.peabot;

import com.ryancarrigan.chatman.Event;

/**
 * Created by Suave Peanut on 2015.1.17.
 */
public class EventService extends ServiceQueue<Event> {

    EventService(final String name, final String channel) {
        super(name, channel);
    }

    @Override
    public void run() {
        // Check if there are events waiting to be logged.
        if (this.hasItems()) {
            // Send all queued events to the database.
            datman().insertEvents(getQueue());
            log.info(String.format("Recorded %d events.", size()));

            // Clear the queue after events have been logged.
            this.clear();
        }
    }

}
