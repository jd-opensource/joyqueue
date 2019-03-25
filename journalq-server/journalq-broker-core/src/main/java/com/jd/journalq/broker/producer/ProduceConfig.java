package com.jd.journalq.broker.producer;

import com.jd.journalq.toolkit.config.PropertySupplier;

/**
 * Created by chengzhiliang on 2018/10/30.
 */
public class ProduceConfig {
    //TODO
    private PropertySupplier propertySupplier;

    public ProduceConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }


    public int getFeedbackTimeout() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.FEEDBACK_TIMEOUT);
    }

    public int getTransactionExpireClearInterval() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.TRANSACTION_CLEAR_INTERVAL);
    }

    public int getTransactionExpireTime() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.TRANSACTION_EXPIRE_TIME);
    }

    public int getFixThreadPoolNThreads() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.FIX_THREAD_POOL_THREADS);
    }

    public int getBrokerQosLevel() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.BROKER_QOS_LEVEL);
    }
}