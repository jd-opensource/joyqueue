package io.chubao.joyqueue.nsr.config;

import io.chubao.joyqueue.toolkit.config.PropertyDef;

/**
 * MessengerConfigKey
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public enum MessengerConfigKey implements PropertyDef {

    SESSION_EXPIRE_TIME("nameserver.messenger.session.expire.time", 1000 * 60 * 5, Type.INT),
    SESSION_TIMEOUT("nameserver.messenger.session.timeout", 1000 * 5, Type.INT),
    PUBLISH_TIMEOUT("nameserver.messenger.publish.timeout", 1000 * 10, Type.INT),
    PUBLISH_FORCE("nameserver.messenger.publish.force", false, Type.BOOLEAN),
    HEARTBEAT_INTERVAL("nameserver.messenger.heartbeat.interval", 1000 * 10, Type.INT),
    HEARTBEAT_TIMEOUT("nameserver.messenger.heartbeat.timeout", 300, Type.INT),
    PORT("nameserver.messenger.port", 50093, Type.INT);

    public static final String MESSENGER_SERVER_CONFIG_PREFIX = "nameserver.messenger.server.";
    public static final String MESSENGER_CLIENT_CONFIG_PREFIX = "nameserver.messenger.client.";

    private String name;
    private Object value;
    private PropertyDef.Type type;

    MessengerConfigKey(String name, Object value, PropertyDef.Type type) {
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
