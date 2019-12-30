package org.joyqueue.broker.network.session;

import org.joyqueue.toolkit.config.PropertyDef;

/**
 * @author LiYue
 * Date: 2019/12/12
 */
public enum BrokerTransportConfigKey  implements PropertyDef {
    // session同步超时
    SESSION_SYNC_TIMEOUT("broker.transport.session.sync.timeout", 1000 * 3, PropertyDef.Type.INT),
    // session缓存时间
    SESSION_EXPIRE_TIME("broker.transport.session.expire.time", 1000 * 60 * 10, PropertyDef.Type.INT);

    private String name;
    private Object value;
    private PropertyDef.Type type;

    BrokerTransportConfigKey(String name, Object value, PropertyDef.Type type) {
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
