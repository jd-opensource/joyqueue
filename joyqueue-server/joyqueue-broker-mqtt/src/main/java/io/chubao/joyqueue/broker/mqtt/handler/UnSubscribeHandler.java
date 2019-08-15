package io.chubao.joyqueue.broker.mqtt.handler;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * @author majun8
 */
public class UnSubscribeHandler extends Handler implements ExecutorsProvider {
    private static final Logger logger = LoggerFactory.getLogger(UnSubscribeHandler.class);

    @Override
    public void handleRequest(Channel client, MqttMessage message) throws Exception {
        MqttUnsubscribeMessage unSubscribeMessage = (MqttUnsubscribeMessage) message;

        mqttProtocolHandler.processUnsubscribe(client, unSubscribeMessage);
    }

    @Override
    public MqttMessageType type() {
        return MqttMessageType.UNSUBSCRIBE;
    }

    @Override
    public ExecutorService getExecutorService() {
        return mqttContext.getExecutorServiceMap().get(UnSubscribeHandler.class);
    }
}
