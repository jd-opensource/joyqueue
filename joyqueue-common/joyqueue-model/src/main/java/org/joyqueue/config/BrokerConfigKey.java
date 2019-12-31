package org.joyqueue.config;

import org.joyqueue.toolkit.config.PropertyDef;

/**
 * BrokerConfigKeys
 * author: gaohaoxiang
 * date: 2019/12/6
 */
public enum BrokerConfigKey implements PropertyDef {

    FRONTEND_SERVER_PORT("broker.frontend-server.transport.server.port", 50088, Type.INT),
    FRONTEND_SERVER_COMMON_THREADS("broker.frontend-server.common.threads", Runtime.getRuntime().availableProcessors() * 4, Type.INT),
    FRONTEND_SERVER_COMMON_THREAD_QUEUE_SIZE("broker.frontend-server.common.thread.queue.size", 102400, Type.INT),
    FRONTEND_SERVER_FETCH_THREADS("broker.frontend-server.fetch.threads", Runtime.getRuntime().availableProcessors() * 4, Type.INT),
    FRONTEND_SERVER_FETCH_THREAD_QUEUE_SIZE("broker.frontend-server.fetch.thread.queue.size", 102400, Type.INT),
    FRONTEND_SERVER_PRODUCE_THREADS("broker.frontend-server.produce.threads", Runtime.getRuntime().availableProcessors() * 4, Type.INT),
    FRONTEND_SERVER_PRODUCE_THREAD_QUEUE_SIZE("broker.frontend-server.produce.thread.queue.size", 102400, Type.INT),

    ;

    private String name;
    private Object value;
    private PropertyDef.Type type;

    BrokerConfigKey(String name, Object value, PropertyDef.Type type) {
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