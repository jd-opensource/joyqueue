package io.chubao.joyqueue.nsr.config;

import io.chubao.joyqueue.toolkit.config.PropertyDef;

/**
 * MessengerConfigKey
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public enum MessengerConfigKey implements PropertyDef {

    SESSION_EXPIRE_TIME("nameservice.messenger.session.expire.time", 1000 * 60 * 5, Type.INT),
    SESSION_TIMEOUT("nameservice.messenger.session.timeout", 1000 * 5, Type.INT),
    PUBLISH_TIMEOUT("nameservice.messenger.publish.timeout", 1000 * 10, Type.INT),
    PUBLISH_FORCE("nameservice.messenger.publish.force", false, Type.BOOLEAN),
    PUBLISH_IGNORE_CONNECTION_ERROR("nameservice.messenger.publish.ignore.connection.error", true, Type.BOOLEAN),
    HEARTBEAT_INTERVAL("nameservice.messenger.heartbeat.interval", 1000 * 10, Type.INT),
    HEARTBEAT_TIMEOUT("nameservice.messenger.heartbeat.timeout", 300, Type.INT),
    PORT("nameservice.messenger.port", 50093, Type.INT);

    public static final String MESSENGER_SERVER_CONFIG_PREFIX = "nameservice.messenger.server.";
    public static final String MESSENGER_CLIENT_CONFIG_PREFIX = "nameservice.messenger.client.";

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
