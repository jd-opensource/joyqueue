package com.jd.journalq.broker.mqtt.handler;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * @author majun8
 */
public class PublishHandler extends Handler implements ExecutorsProvider {
    private static final Logger LOG = LoggerFactory.getLogger(PublishHandler.class);

    public PublishHandler() {

    }

    @Override
    public void handleRequest(Channel client, MqttMessage message) throws Exception {
        MqttPublishMessage publishMessage = (MqttPublishMessage) message;

        mqttProtocolHandler.processPublish(client, publishMessage);
    }

    @Override
    public MqttMessageType type() {
        return MqttMessageType.PUBLISH;
    }

    @Override
    public ExecutorService getExecutorService() {
        return mqttContext.getExecutorServiceMap().get(PublishHandler.class);
    }
}
