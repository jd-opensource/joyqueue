package com.jd.journalq.broker.jmq.config;

import com.jd.journalq.toolkit.config.PropertyDef;

/**
 * JMQConfigKey
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/3
 */
public enum JMQConfigKey implements PropertyDef {

    // 协调者分区分配类型
    COORDINATOR_PARTITION_ASSIGN_TYPE("jmq.coordinator.partition.assign.type", "PARTITION_GROUP_BALANCE", PropertyDef.Type.STRING),

    // 协调者分配超时溢出
    COORDINATOR_PARTITION_ASSIGN_TIMEOUT_OVERFLOW("jmq.coordinator.partition.assign.timeout.overflow", 1000 * 60 * 1, PropertyDef.Type.INT),

    // 协调者分区分配最小连接数
    COORDINATOR_PARTITION_ASSIGN_MIN_CONNECTIONS("jmq.coordinator.partition.assign.minConnections", 1, PropertyDef.Type.INT),

    // 生产最大超时
    PRODUCE_MAX_TIMEOUT("jmq.producer.max.timeout", 1000 * 60, PropertyDef.Type.INT),


    ;

    private String name;
    private Object value;
    private PropertyDef.Type type;

     JMQConfigKey(String name, Object value, PropertyDef.Type type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public PropertyDef.Type getType() {
        return type;
    }
}