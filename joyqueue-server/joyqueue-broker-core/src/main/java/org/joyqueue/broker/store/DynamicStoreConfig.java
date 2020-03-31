package org.joyqueue.broker.store;

/**
 * Dynamic store config interface
 *
 **/
public interface DynamicStoreConfig {
    /**
     * Topic  store max time of log
     *
     **/
    long storeLogMaxTime(String topic);

    /**
     * Topic Keep unconsumed log
     * @return true if keep unconsumed log
     **/
    boolean keepUnconsumed(String topic);
}
