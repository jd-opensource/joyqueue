package com.jd.journalq.broker.mqtt.handler;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author majun8
 */
public class ConnectAckHandler extends Handler {
    private static final Logger logger = LoggerFactory.getLogger(ConnectAckHandler.class);

    @Override
    public void handleRequest(Channel client, MqttMessage message) throws Exception {

    }

    @Override
    public MqttMessageType type() {
        return MqttMessageType.CONNACK;
    }
}
