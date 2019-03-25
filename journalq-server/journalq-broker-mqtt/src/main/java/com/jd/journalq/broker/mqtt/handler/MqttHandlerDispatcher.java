package com.jd.journalq.broker.mqtt.handler;

import com.jd.journalq.broker.mqtt.config.MqttConfig;
import com.jd.journalq.broker.mqtt.config.MqttContext;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.common.network.transport.command.handler.CommandHandlerFactory;
import com.jd.journalq.broker.mqtt.command.MqttHandlerFactory;
import com.jd.journalq.toolkit.service.Service;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author majun8
 */
public class MqttHandlerDispatcher extends Service {
    private final Logger logger = LoggerFactory.getLogger(MqttHandlerDispatcher.class);

    private Map<MqttMessageType, Handler> handlerMap = new HashMap<>();

    private MqttHandlerFactory handlerFactory;
    private MqttProtocolHandler mqttProtocolHandler;
    private MqttContext mqttContext;

    public MqttHandlerDispatcher(CommandHandlerFactory handlerFactory, BrokerContext brokerContext) {
        this.handlerFactory = (MqttHandlerFactory) handlerFactory;
        this.mqttProtocolHandler = new MqttProtocolHandler(brokerContext);
        this.mqttContext = new MqttContext(new MqttConfig(brokerContext.getPropertySupplier()));
    }

    public Handler getHandler(MqttMessageType type) {
        return handlerMap.get(type);
    }


    @Override
    protected void doStart() throws Exception {
        super.doStart();
        init();
        mqttContext.start();
        mqttProtocolHandler.start();
    }

    public void init() {
        for (Handler handler : handlerFactory.getHandlers()) {
            handler.setMqttProtocolHandler(mqttProtocolHandler);
            handler.setMqttContext(mqttContext);
            handlerMap.put(handler.type(), handler);
        }
        logger.info("MqttHandlerDispatcher started!");
    }

    public MqttProtocolHandler getMqttProtocolHandler() {
        return mqttProtocolHandler;
    }
}
