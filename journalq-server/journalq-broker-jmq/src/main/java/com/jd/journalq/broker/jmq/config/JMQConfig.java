package com.jd.journalq.broker.jmq.config;

import com.jd.journalq.toolkit.config.PropertySupplier;

/**
 * JMQConfig
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/3
 */
public class JMQConfig {
    protected PropertySupplier propertySupplier;

    public JMQConfig() {

    }

    public JMQConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public int getProduceMaxTimeout() {
        return PropertySupplier.getValue(propertySupplier, JMQConfigKey.PRODUCE_MAX_TIMEOUT);
    }

    public String getCoordinatorPartitionAssignType() {
        return PropertySupplier.getValue(propertySupplier, JMQConfigKey.COORDINATOR_PARTITION_ASSIGN_TYPE);
    }

    public int getCoordinatorPartitionAssignMinConnections() {
        return PropertySupplier.getValue(propertySupplier, JMQConfigKey.COORDINATOR_PARTITION_ASSIGN_MIN_CONNECTIONS);
    }

    public int getCoordinatorPartitionAssignTimeoutOverflow() {
        return PropertySupplier.getValue(propertySupplier, JMQConfigKey.COORDINATOR_PARTITION_ASSIGN_TIMEOUT_OVERFLOW);
    }
}