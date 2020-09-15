package org.joyqueue.broker.joyqueue0.config;

import org.joyqueue.toolkit.config.PropertyDef;

/**
 * Joyqueue0ConfigKey
 * author: gaohaoxiang
 * date: 2019/11/12
 */
public enum Joyqueue0ConfigKey implements PropertyDef {

    CLUSTER_BODY_CACHE_ENABLE("joyqueue0.cluster.body.cache.enable", false, Type.BOOLEAN),
    CLUSTER_BODY_CACHE_EXPIRE_TIME("joyqueue0.cluster.body.cache.expire.time", 1000 * 30, Type.INT),
    CLUSTER_BODY_CACHE_UPDATE_INTERVAL("joyqueue0.cluster.body.update.interval", 1000 * 30, Type.INT),
    CLUSTER_BODY_WITH_SLAVE("joyqueue0.cluster.body.with.slave", false, Type.BOOLEAN),
    MESSAGE_BUSINESSID_REWRITE_PREFIX("joyqueue0.message.businessId.rewrite.", false, Type.BOOLEAN),

    ;


    private String name;
    private Object value;
    private PropertyDef.Type type;

    Joyqueue0ConfigKey(String name, Object value, PropertyDef.Type type) {
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