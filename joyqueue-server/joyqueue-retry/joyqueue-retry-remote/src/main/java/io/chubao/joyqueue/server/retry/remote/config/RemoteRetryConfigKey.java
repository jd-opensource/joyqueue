package io.chubao.joyqueue.server.retry.remote.config;

import io.chubao.joyqueue.toolkit.config.PropertyDef;

/**
 * @author liyue25
 * Date: 2019-07-05
 */
public enum  RemoteRetryConfigKey implements PropertyDef {

    REMOTE_RETRY_LIMIT_THREADS("retry.remote.retry.limit.thread", 10, Type.INT),
    REMOTE_RETRY_UPDATE_INTERVAL("retry.remote.retry.update.interval", 60000L, Type.LONG);


    private String name;
    private Object value;
    private PropertyDef.Type type;

    RemoteRetryConfigKey(String name, Object value, PropertyDef.Type type) {
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
