package com.jd.journalq.broker.mqtt.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author majun8
 */
public class PollSelector implements Selector {
    private AtomicInteger count = new AtomicInteger(0);

    @Override
    public int select(String selector, int totalSize) {
        return count.getAndIncrement() % totalSize;
    }
}
