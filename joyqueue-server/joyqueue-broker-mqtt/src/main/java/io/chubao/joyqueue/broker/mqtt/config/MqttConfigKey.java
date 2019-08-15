package io.chubao.joyqueue.broker.mqtt.config;

import io.chubao.joyqueue.toolkit.config.PropertyDef;

/**
 * @author majun8
 */
public enum MqttConfigKey implements PropertyDef {

    EXECUTOR_SERVICE_CONNECTION("mqtt.executor.service.connection.name", "connect-executors", Type.STRING),
    EXECUTOR_SERVICE_PING("mqtt.executor.service.ping.name", "ping-executors", Type.STRING),
    EXECUTOR_SERVICE_SUBSCRIPTION("mqtt.executor.service.subscription.name", "subscription-executors", Type.STRING),
    EXECUTOR_SERVICE_PUBLISH("mqtt.executor.service.publish.name", "publish-executors", Type.STRING),
    EXECUTOR_SERVICE_CONNECTION_THREAD("mqtt.executor.service.connection.threads", Runtime.getRuntime().availableProcessors() * 2, Type.INT),
    EXECUTOR_SERVICE_PING_THREAD("mqtt.executor.service.ping.threads", Runtime.getRuntime().availableProcessors() * 2, Type.INT),
    EXECUTOR_SERVICE_SUBSCRIPTION_THREAD("mqtt.executor.service.subscription.threads", Runtime.getRuntime().availableProcessors(), Type.INT),
    EXECUTOR_SERVICE_PUBLISH_THREAD("mqtt.executor.service.publish.threads", Runtime.getRuntime().availableProcessors() * 3, Type.INT),
    EXECUTOR_SERVICE_CONNECTION_QUEUESIZE("mqtt.executor.service.connection.queuesize", 10000, Type.INT),
    EXECUTOR_SERVICE_PING_QUEUESIZE("mqtt.executor.service.ping.queuesize", 10000, Type.INT),
    EXECUTOR_SERVICE_SUBSCRIPTION_QUEUESIZE("mqtt.executor.service.subscription.queuesize", 10000, Type.INT),
    EXECUTOR_SERVICE_PUBLISH_QUEUESIZE("mqtt.executor.service.publish.queuesize", 10000, Type.INT);

    private String name;
    private Object value;
    private Type type;

    MqttConfigKey(String name, Object value, Type type) {
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
