package com.ryancarrigan.peabot;

import com.ryancarrigan.data.Dataman;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Suave Peanut on 2015.1.17.
 */
abstract class ServiceQueue<E> implements Runnable {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final String channel;
    protected final String name;
    private final Queue<E> queue;

    protected ServiceQueue(final String name, final String channel) {
        this.channel = channel;
        this.name    = name;
        this.queue   = new LinkedList<>();
    }

    public void add(final E item) {
        this.queue.add(item);
    }

    protected void clear() {
        this.queue.clear();
    }

    protected Dataman datman() {
        return new Dataman(name, channel);
    }

    public Queue<E> getQueue() {
        return queue;
    }

    public boolean hasItems() {
        return size() > 0;
    }

    public int size() {
        return this.queue.size();
    }

    public void dumpQueue() {
        if (this.hasItems()) {
            try {
                run();
                if (size() > 0) {
                    log.info("Some elements stayed in memory. Writing them to file.");
                    writeFile();
                }
            } catch (IOException e) {
                log.error("Unable to empty queue.", e);
            }
        }
    }

    private void writeFile() throws IOException {
        final File dumpFile       = new File(getClass().getSimpleName() + "_dump.txt");
        if (!dumpFile.exists() && dumpFile.createNewFile()) {
            log.error("Could not create log file: " + dumpFile.getName());
        }
        final OutputStream output = new FileOutputStream(dumpFile, true);
        final Writer writer       = new OutputStreamWriter(output, Charset.forName("UTF-8"));

        for (final E item : queue) writer.write(item.toString() + "\n");
        writer.close();
    }

}
