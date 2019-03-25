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

    public static final String GROUP_KAFKA_COORDINATOR = "coordinator";
    public static final String CONFIG_GROUP = GROUP_KAFKA_COORDINATOR;

    protected static final Logger logger = LoggerFactory.getLogger(CoordinatorConfig.class);

    private PropertySupplier propertySupplier;

    public CoordinatorConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public TopicName getTopic() {
        return TopicName.parse(PropertySupplier.getValue(propertySupplier, CoordinatorConfigKey.TOPIC_CODE));
    }

    public short getTopicPartitions() {
        return PropertySupplier.getValue(propertySupplier, CoordinatorConfigKey.TOPIC_PARTITIONS);
    }

    public int getGroupExpireTime() {
        return PropertySupplier.getValue(propertySupplier, CoordinatorConfigKey.GROUP_EXPIRE_TIME);
    }
}