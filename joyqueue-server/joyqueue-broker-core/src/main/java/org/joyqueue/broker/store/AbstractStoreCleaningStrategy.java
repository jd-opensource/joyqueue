package org.joyqueue.broker.store;

import org.joyqueue.broker.config.BrokerStoreConfig;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.toolkit.config.PropertySupplier;

/**
 *
 * Base store clean strategy with dynamic config
 *
 **/
public abstract class AbstractStoreCleaningStrategy implements StoreCleaningStrategy,DynamicStoreConfig {

    private BrokerStoreConfig brokerStoreConfig;
    @Override
    public long storeLogMaxTime(TopicConfig topicConfig) {
        if (topicConfig != null) {
            if (topicConfig.getPolicy() != null && topicConfig.getPolicy().getStoreMaxTime() != null) {
                return topicConfig.getPolicy().getStoreMaxTime();
            } else {
                return brokerStoreConfig.getMaxStoreTime(topicConfig.getName().getFullName());
            }
        }
        return brokerStoreConfig.getMaxStoreTime();
    }

    @Override
    public boolean keepUnconsumed(TopicConfig topicConfig) {
        if (topicConfig != null) {
            if (topicConfig.getPolicy() != null && topicConfig.getPolicy().getStoreCleanKeepUnconsumed() != null) {
                return topicConfig.getPolicy().getStoreCleanKeepUnconsumed();
            } else {
                return brokerStoreConfig.keepUnconsumed(topicConfig.getName().getFullName());
            }
        }
        return brokerStoreConfig.keepUnconsumed();
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
         this.brokerStoreConfig = new BrokerStoreConfig(supplier);
    }
}
