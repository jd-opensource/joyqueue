package io.chubao.joyqueue.broker.mqtt.handler;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * @author majun8
 */
public class PublishAckHandler extends Handler implements ExecutorsProvider {
    private static final Logger logger = LoggerFactory.getLogger(PublishAckHandler.class);

    @Override
    public void handleRequest(Channel client, MqttMessage message) throws Exception {
        MqttPubAckMessage pubAckMessage = (MqttPubAckMessage) message;

        mqttProtocolHandler.processPubAck(client, pubAckMessage);
    }

    @Override
    public MqttMessageType type() {
        return MqttMessageType.PUBACK;
    }

    @Override
    public ExecutorService getExecutorService() {
        return mqttContext.getExecutorServiceMap().get(PublishAckHandler.class);
    }
}
