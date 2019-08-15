package io.chubao.joyqueue.broker.mqtt.handler;

import io.chubao.joyqueue.broker.mqtt.config.MqttContext;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;

/**
 * @author majun8
 */
public abstract class Handler {
    protected MqttProtocolHandler mqttProtocolHandler;
    protected MqttContext mqttContext;

    public abstract void handleRequest(Channel client, MqttMessage message) throws Exception;
    public abstract MqttMessageType type();

    public void setMqttContext(MqttContext mqttContext) {
        this.mqttContext = mqttContext;
    }

    public void setMqttProtocolHandler(MqttProtocolHandler mqttProtocolHandler) {
        this.mqttProtocolHandler = mqttProtocolHandler;
    }
}
