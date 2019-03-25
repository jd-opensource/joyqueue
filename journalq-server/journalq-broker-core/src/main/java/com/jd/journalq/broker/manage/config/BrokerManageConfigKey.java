package com.jd.journalq.broker.manage.config;

import com.jd.journalq.toolkit.config.PropertyDef;
import com.jd.journalq.toolkit.network.IpUtil;

/**
 * broker监控配置key
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/10
 */
public enum BrokerManageConfigKey implements PropertyDef {

    EXPORT_HOST("manager.export.host", IpUtil.getLocalIp(), PropertyDef.Type.STRING),
    EXPORT_PORT("manager.export.port", 50090, PropertyDef.Type.INT);

    private String name;
    private Object value;
    private PropertyDef.Type type;

    private BrokerManageConfigKey(String name, Object value, PropertyDef.Type type) {
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