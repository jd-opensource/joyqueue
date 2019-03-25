package com.jd.journalq.broker.monitor.config;

import com.jd.journalq.toolkit.config.PropertyDef;

/**
 * broker监控配置key
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/10
 */
public enum BrokerMonitorConfigKey implements PropertyDef {

    ENABLE("stat.enable", true, PropertyDef.Type.BOOLEAN),
    STAT_SAVE_FILE("stat.save.file", "/store/stat", PropertyDef.Type.STRING),
    STAT_SAVE_INTERVAL("stat.save.interval", 1000 * 30, PropertyDef.Type.INT),
    WRITER_THREAD("writer.thread", 5, PropertyDef.Type.INT),
    WRITER_SIZE("writer.size", 10240, PropertyDef.Type.INT);

    private String name;
    private Object value;
    private PropertyDef.Type type;

    BrokerMonitorConfigKey(String name, Object value, PropertyDef.Type type) {
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