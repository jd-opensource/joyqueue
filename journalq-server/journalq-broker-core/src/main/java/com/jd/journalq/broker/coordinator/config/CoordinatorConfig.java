package com.jd.journalq.broker.coordinator.config;

import com.jd.journalq.domain.TopicName;
import com.jd.journalq.toolkit.config.PropertySupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CoordinatorConfig
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class CoordinatorConfig {

    protected static final Logger logger = LoggerFactory.getLogger(CoordinatorConfig.class);

    private PropertySupplier propertySupplier;

    public CoordinatorConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public TopicName getGroupTopic() {
        return TopicName.parse(PropertySupplier.getValue(propertySupplier, CoordinatorConfigKey.GROUP_TOPIC_CODE));
    }

    public short getGroupTopicPartitions() {
        return PropertySupplier.getValue(propertySupplier, CoordinatorConfigKey.GROUP_TOPIC_PARTITIONS);
    }

    public int getGroupExpireTime() {
        return PropertySupplier.getValue(propertySupplier, CoordinatorConfigKey.GROUP_EXPIRE_TIME);
    }

    public int getGroupMaxNum() {
        return PropertySupplier.getValue(propertySupplier, CoordinatorConfigKey.GROUP_MAX_NUM);
    }

    public TopicName getTransactionTopic() {
        return TopicName.parse(PropertySupplier.getValue(propertySupplier, CoordinatorConfigKey.TRANSACTION_TOPIC_CODE));
    }

    public short getTransactionTopicPartitions() {
        return PropertySupplier.getValue(propertySupplier, CoordinatorConfigKey.TRANSACTION_TOPIC_PARTITIONS);
    }

    public int getTransactionExpireTime() {
        return PropertySupplier.getValue(propertySupplier, CoordinatorConfigKey.TRANSACTION_EXPIRE_TIME);
    }

    public int getTransactionMaxNum() {
        return PropertySupplier.getValue(propertySupplier, CoordinatorConfigKey.GROUP_MAX_NUM);
    }
}