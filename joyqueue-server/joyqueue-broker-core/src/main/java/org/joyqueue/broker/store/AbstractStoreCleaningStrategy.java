package org.joyqueue.broker.store;

import org.joyqueue.broker.config.BrokerStoreConfig;
import org.joyqueue.toolkit.config.PropertySupplier;

/**
 *
 * Base store clean strategy with dynamic config
 *
 **/
public abstract class AbstractStoreCleaningStrategy implements StoreCleaningStrategy, DynamicStoreConfig {

    private BrokerStoreConfig brokerStoreConfig;
    @Override
    public long storeLogMaxTime(String topic) {
        return brokerStoreConfig.getMaxStoreTime(topic);
    }

    @Override
    public boolean keepUnconsumed(String topic) {
        return brokerStoreConfig.keepUnconsumed(topic);
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
         this.brokerStoreConfig = new BrokerStoreConfig(supplier);
    }
}
