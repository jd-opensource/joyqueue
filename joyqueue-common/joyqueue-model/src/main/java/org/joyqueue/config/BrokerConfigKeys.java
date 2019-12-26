package org.joyqueue.config;

import org.joyqueue.toolkit.config.PropertyDef;

/**
 * BrokerConfigKeys
 * author: gaohaoxiang
 * date: 2019/12/6
 */
public enum BrokerConfigKeys implements PropertyDef {

    FRONTEND_SERVER_PORT("broker.frontend-server.transport.server.port", 50088, Type.INT),

    ;

    private String name;
    private Object value;
    private PropertyDef.Type type;

    BrokerConfigKeys(String name, Object value, PropertyDef.Type type) {
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