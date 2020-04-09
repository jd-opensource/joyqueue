package org.joyqueue.broker.mqtt.network;

import io.netty.channel.ChannelInitializer;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.mqtt.config.MqttConfig;
import org.joyqueue.broker.mqtt.config.MqttContext;
import org.joyqueue.broker.mqtt.handler.MqttHandlerDispatcher;
import org.joyqueue.broker.mqtt.transport.MqttCommandInvocation;

public abstract class AbstractMqttProtocolPipeline extends ChannelInitializer {
    protected MqttContext mqttContext;

    public AbstractMqttProtocolPipeline(BrokerContext brokerContext) {
        this.mqttContext = new MqttContext(new MqttConfig(brokerContext.getPropertySupplier()));
    }

    protected abstract MqttCommandInvocation newMqttCommandInvocation();

    protected abstract MqttHandlerDispatcher newMqttHandlerDispatcher();
}
