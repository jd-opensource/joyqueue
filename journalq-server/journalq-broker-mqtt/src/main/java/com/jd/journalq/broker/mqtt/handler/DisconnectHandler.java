package com.jd.journalq.broker.mqtt.handler;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author majun8
 */
public class DisconnectHandler extends Handler {
    private final static Logger logger = LoggerFactory.getLogger(DisconnectHandler.class);

    public DisconnectHandler() {

    }

    @Override
    public void handleRequest(Channel client, MqttMessage message) throws Exception {
        mqttProtocolHandler.processDisconnect(client);
    }

    @Override
    public MqttMessageType type() {
        return MqttMessageType.DISCONNECT;
    }
}
