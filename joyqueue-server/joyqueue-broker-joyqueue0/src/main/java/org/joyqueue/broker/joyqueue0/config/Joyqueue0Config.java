package org.joyqueue.broker.joyqueue0.config;

import org.joyqueue.toolkit.config.PropertySupplier;

/**
 * Joyqueue0Config
 * author: gaohaoxiang
 * date: 2019/11/12
 */
public class Joyqueue0Config {

    private PropertySupplier propertySupplier;

    public Joyqueue0Config(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public boolean getClusterBodyCacheEnable() {
        return PropertySupplier.getValue(propertySupplier, Joyqueue0ConfigKey.CLUSTER_BODY_CACHE_ENABLE);
    }

    public int getClusterBodyCacheExpireTime() {
        return PropertySupplier.getValue(propertySupplier, Joyqueue0ConfigKey.CLUSTER_BODY_CACHE_EXPIRE_TIME);
    }

    public int getClusterBodyCacheUpdateInterval() {
        return PropertySupplier.getValue(propertySupplier, Joyqueue0ConfigKey.CLUSTER_BODY_CACHE_UPDATE_INTERVAL);
    }

    public boolean getClusterBodyWithSlave() {
        return PropertySupplier.getValue(propertySupplier, Joyqueue0ConfigKey.CLUSTER_BODY_WITH_SLAVE);
    }

    public boolean getMessageBusinessIdRewrite(String topic) {
        return (boolean) PropertySupplier.getValue(propertySupplier,
                Joyqueue0ConfigKey.MESSAGE_BUSINESSID_REWRITE_PREFIX.getName() + topic,
                Joyqueue0ConfigKey.MESSAGE_BUSINESSID_REWRITE_PREFIX.getType(),
                Joyqueue0ConfigKey.MESSAGE_BUSINESSID_REWRITE_PREFIX.getValue());
    }
}