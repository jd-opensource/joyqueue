package io.chubao.joyqueue.broker.mqtt.handler;

import io.chubao.joyqueue.broker.mqtt.config.MqttConfig;
import io.chubao.joyqueue.broker.mqtt.config.MqttContext;
import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import io.chubao.joyqueue.broker.mqtt.command.MqttHandlerFactory;
import io.chubao.joyqueue.toolkit.service.Service;
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
