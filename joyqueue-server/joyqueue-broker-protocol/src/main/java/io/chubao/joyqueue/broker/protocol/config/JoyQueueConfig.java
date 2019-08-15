package io.chubao.joyqueue.broker.protocol.config;

import io.chubao.joyqueue.toolkit.config.PropertySupplier;

/**
 * JoyQueueConfig
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/3
 */
public class JoyQueueConfig {
    protected PropertySupplier propertySupplier;

    public JoyQueueConfig() {

    }

    public JoyQueueConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public int getProduceMaxTimeout() {
        return PropertySupplier.getValue(propertySupplier, JoyQueueConfigKey.PRODUCE_MAX_TIMEOUT);
    }

    public String getCoordinatorPartitionAssignType() {
        return PropertySupplier.getValue(propertySupplier, JoyQueueConfigKey.COORDINATOR_PARTITION_ASSIGN_TYPE);
    }

    public int getCoordinatorPartitionAssignMinConnections() {
        return PropertySupplier.getValue(propertySupplier, JoyQueueConfigKey.COORDINATOR_PARTITION_ASSIGN_MIN_CONNECTIONS);
    }

    public int getCoordinatorPartitionAssignTimeoutOverflow() {
        return PropertySupplier.getValue(propertySupplier, JoyQueueConfigKey.COORDINATOR_PARTITION_ASSIGN_TIMEOUT_OVERFLOW);
    }
}