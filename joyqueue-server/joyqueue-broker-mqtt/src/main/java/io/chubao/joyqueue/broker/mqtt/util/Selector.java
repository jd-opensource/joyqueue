package io.chubao.joyqueue.broker.mqtt.util;

/**
 * @author majun8
 */
public interface Selector {
    int select(String selector, int totalSize);
}
