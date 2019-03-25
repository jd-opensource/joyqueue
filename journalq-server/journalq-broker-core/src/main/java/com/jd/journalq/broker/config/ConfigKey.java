package com.jd.journalq.broker.config;

import com.jd.journalq.common.context.ConfigDef;

public enum ConfigKey {
    ;
    private String name;
    private Object value;
    private ConfigDef.Type type;

    ConfigKey(String name, Object value, ConfigDef.Type type) {
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

    public ConfigDef.Type getType() {
        return type;
    }
}
