package com.jd.journalq.nsr.config;

import com.jd.journalq.toolkit.config.PropertyDef;

public enum NameServiceConfigKey implements PropertyDef {
    NAMESERVER_ADDRESS("nameservice.serverAddress", "local", Type.STRING);
    public static final String NAMESERVICE_KEY_PREFIX ="nameservice.";

    private String name;
    private Object value;
    private PropertyDef.Type type;

    NameServiceConfigKey(String name, Object value, PropertyDef.Type type) {
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
