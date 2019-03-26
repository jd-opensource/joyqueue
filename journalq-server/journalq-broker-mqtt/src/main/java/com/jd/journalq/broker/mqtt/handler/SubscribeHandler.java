package com.jd.journalq.broker.mqtt.handler;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * @author majun8
 */
public class SubscribeHandler extends Handler implements ExecutorsProvider {
    private static final Logger LOG = LoggerFactory.getLogger(SubscribeHandler.class);

    public SubscribeHandler() {

    }

    @Override
    public void handleRequest(Channel client, MqttMessage message) throws Exception {
        MqttSubscribeMessage subscribeMessage = (MqttSubscribeMessage) message;

        mqttProtocolHandler.processSubscribe(client, subscribeMessage);
    }

    @Override
    public MqttMessageType type() {
        return MqttMessageType.SUBSCRIBE;
    }

    @Override
    public ExecutorService getExecutorService() {
        return mqttContext.getExecutorServiceMap().get(SubscribeHandler.class);
    }
}
