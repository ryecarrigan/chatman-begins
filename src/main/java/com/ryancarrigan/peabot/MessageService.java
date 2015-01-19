package com.ryancarrigan.peabot;

import com.ryancarrigan.chatman.IrcBot;

/**
 * Created by Suave Peanut on 2015.1.17.
 */
public class MessageService extends ServiceQueue<String> {

    private IrcBot bot;

    protected MessageService(final IrcBot bot) {
        super(bot.getName(), bot.getChannel());
        this.bot = bot;
    }

    @Override
    public void run() {
        // Check if there is a message in the queue.
        final String message = getQueue().poll();
        if (message != null) {
            log.info(String.format("Sending message: %s %s> %s", channel, name, message));

            // Send a message to the current channel.
            bot.sendMessage(channel, message);
        }
    }
}
