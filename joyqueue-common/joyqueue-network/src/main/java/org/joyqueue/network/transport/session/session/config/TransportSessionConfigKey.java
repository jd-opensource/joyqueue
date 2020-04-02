package org.joyqueue.network.transport.session.session.config;

import org.joyqueue.toolkit.config.PropertyDef;

/**
 * TransportSessionConfigKey
 *
 * author: gaohaoxiang
 * date: 2018/11/9
 */
public enum TransportSessionConfigKey implements PropertyDef {

    // 重连间隔
    RECONNECT_INTERVAL("session.reconnect.interval", 1000 * 30, Type.INT),
    // 缓存时间
    EXPIRE_TIME("session.expire.time", 1000 * 60 * 10, Type.INT),

    ;

    private String name;
    private Object value;
    private Type type;

    TransportSessionConfigKey(String name, Object value, Type type) {
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

    public Type getType() {
        return type;
    }
}