package com.jd.joyqueue.broker.jmq2.config;

import org.joyqueue.toolkit.config.PropertyDef;

/**
 * JMQ2ConfigKey
 * author: gaohaoxiang
 * date: 2019/11/12
 */
public enum JMQ2ConfigKey implements PropertyDef {

    CLUSTER_BODY_CACHE_ENABLE("jmq2.cluster.body.cache.enable", false, Type.BOOLEAN),
    CLUSTER_BODY_CACHE_EXPIRE_TIME("jmq2.cluster.body.cache.expire.time", 1000 * 30, Type.INT),
    CLUSTER_BODY_CACHE_UPDATE_INTERVAL("jmq2.cluster.body.update.interval", 1000 * 30, Type.INT),
    CLUSTER_BODY_WITH_SLAVE("jmq2.cluster.body.with.slave", false, Type.BOOLEAN),
    MESSAGE_BUSINESSID_REWRITE_PREFIX("jmq2.message.businessId.rewrite.", false, Type.BOOLEAN),

    ;


    private String name;
    private Object value;
    private PropertyDef.Type type;

    JMQ2ConfigKey(String name, Object value, PropertyDef.Type type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return type;
    }
}