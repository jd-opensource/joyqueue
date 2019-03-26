package com.jd.journalq.broker.mqtt.handler;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * @author majun8
 */
public class PublishRelHandler extends Handler implements ExecutorsProvider {
    private static final Logger logger = LoggerFactory.getLogger(PublishRelHandler.class);

    @Override
    public void handleRequest(Channel client, MqttMessage message) throws Exception {

    }

    @Override
    public MqttMessageType type() {
        return MqttMessageType.PUBREL;
    }

    @Override
    public ExecutorService getExecutorService() {
        return mqttContext.getExecutorServiceMap().get(PublishRelHandler.class);
    }
}
