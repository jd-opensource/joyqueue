package com.jd.journalq.nsr.config;

import com.jd.journalq.toolkit.config.PropertyDef;

/**
 * @author lixiaobin6
 * ${time} ${date}
 */
public enum NameServerConfigKey implements PropertyDef {
    NAMESERVICE_NAME("nameserver.nsr.name", "local", Type.STRING),
    NAMESERVER_ADDRESS("nameserver.nsr.address", "127.0.0.1:50092", Type.STRING),
    NAMESERVER_SERVICE_PORT("nameserver.nsr.service.port", 50092, Type.INT),
    NAMESERVER_MANAGE_PORT("nameserver.nsr.manage.port", 50091, Type.INT);

    public static final String NAME_SERVER_CONFIG_PREFIX = "nameserver.";

    private String name;
    private Object value;
    private PropertyDef.Type type;

    NameServerConfigKey(String name, Object value, PropertyDef.Type type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    @Override
    public java.lang.String getName() {
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
