package io.chubao.joyqueue.broker.producer;

import io.chubao.joyqueue.toolkit.config.PropertySupplier;

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

    public int getTransactionMaxUncomplete() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.TRANSACTION_MAX_UNCOMPLETE);
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

    public int getBrokerQosLevel(String topic) {
        return PropertySupplier.getValue(propertySupplier,
                ProducerConfigKey.BROKER_QOS_LEVEL_PREFIX.getName() + topic,
                ProducerConfigKey.BROKER_QOS_LEVEL_PREFIX.getType(),
                ProducerConfigKey.BROKER_QOS_LEVEL_PREFIX.getValue());
    }

    public long getPrintMetricIntervalMs() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.PRINT_METRIC_INTERVAL_MS);
    }

    public int getBusinessIdLength() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.PRODUCE_BUSINESSID_LENGTH);
    }

    public int getBodyLength() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.PRODUCE_BODY_LENGTH);
    }
}