package org.joyqueue.broker.store;

import org.joyqueue.domain.TopicConfig;

/**
 * Dynamic store config interface
 *
 **/
public interface DynamicStoreConfig {
    /**
     * Topic  store max time of log
     *
     **/
    long storeLogMaxTime(TopicConfig topicConfig);

    /**
     * Topic Keep unconsumed log
     * @return true if keep unconsumed log
     **/
    boolean keepUnconsumed(TopicConfig topicConfig);
}
