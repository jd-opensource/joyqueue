package com.jd.journalq.broker.mqtt.util;

/**
 * @author majun8
 */
public interface Selector {
    public int select(String selector, int totalSize);
}
