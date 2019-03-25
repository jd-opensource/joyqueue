package com.jd.journalq.broker.coordinator.config;

import com.jd.journalq.toolkit.config.PropertyDef;

/**
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public enum CoordinatorConfigKey implements PropertyDef {

    // 协调者主题
    TOPIC_CODE("topic.code", "__group_coordinators", PropertyDef.Type.STRING),
    // 协调者主题分区
//    TOPIC_PARTITIONS("topic.partitions", (short) 50, ConfigDef.Type.SHORT),
    TOPIC_PARTITIONS("topic.partitions", (short) 1, PropertyDef.Type.SHORT),

    // 协调者group过期时间
    GROUP_EXPIRE_TIME("group.expire.time", 1000 * 60 * 60 * 1, PropertyDef.Type.INT);

    private String name;
    private Object value;
    private PropertyDef.Type type;

    CoordinatorConfigKey(String name, Object value, PropertyDef.Type type) {
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