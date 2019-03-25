package com.jd.journalq.broker.mqtt.handler;

import com.jd.journalq.broker.mqtt.util.NettyAttrManager;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

import static io.netty.handler.codec.mqtt.MqttQoS.AT_MOST_ONCE;

/**
 * @author majun8
 */
public class PingReqHandler extends Handler implements ExecutorsProvider {
    private final static Logger logger = LoggerFactory.getLogger(PingReqHandler.class);

    @Override
    public void handleRequest(Channel client, MqttMessage message) throws Exception {
        String clientId = NettyAttrManager.getAttrClientId(client);
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("PingRequest clientId:%s", clientId));
        }
        MqttFixedHeader pingHeader = new MqttFixedHeader(
                MqttMessageType.PINGRESP,
                false,
                AT_MOST_ONCE,
                false,
                0);
        MqttMessage pingResp = new MqttMessage(pingHeader);
        client.writeAndFlush(pingResp);
    }

    @Override
    public MqttMessageType type() {
        return MqttMessageType.PINGREQ;
    }

    @Override
    public ExecutorService getExecutorService() {
        return mqttContext.getExecutorServiceMap().get(PingReqHandler.class);
    }
}
