package com.jd.journalq.broker.mqtt.handler;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * @author majun8
 */
public class PublishRecHandler extends Handler implements ExecutorsProvider {
    private final static Logger logger = LoggerFactory.getLogger(PublishRecHandler.class);

    @Override
    public void handleRequest(Channel client, MqttMessage message) throws Exception {

    }

    @Override
    public MqttMessageType type() {
        return MqttMessageType.PUBREC;
    }

    @Override
    public ExecutorService getExecutorService() {
        return mqttContext.getExecutorServiceMap().get(PublishRecHandler.class);
    }
}
