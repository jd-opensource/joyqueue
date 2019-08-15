package io.chubao.joyqueue.broker.mqtt.handler;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * @author majun8
 */
public class ConnectHandler extends Handler implements ExecutorsProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectHandler.class);

    public ConnectHandler() {

    }

    @Override
    public void handleRequest(Channel client, MqttMessage message) throws Exception {
        MqttConnectMessage connectMessage = (MqttConnectMessage) message;

        mqttProtocolHandler.processConnect(client, connectMessage);
    }

    @Override
    public MqttMessageType type() {
        return MqttMessageType.CONNECT;
    }

    @Override
    public ExecutorService getExecutorService() {
        return mqttContext.getExecutorServiceMap().get(ConnectHandler.class);
    }
}
