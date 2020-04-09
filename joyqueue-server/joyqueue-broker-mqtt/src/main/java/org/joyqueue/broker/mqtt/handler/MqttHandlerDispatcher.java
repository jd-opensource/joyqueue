/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.mqtt.handler;

import org.joyqueue.broker.mqtt.config.MqttContext;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.mqtt.command.MqttHandlerFactory;
import org.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import org.joyqueue.toolkit.service.Service;
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

    public MqttHandlerDispatcher(CommandHandlerFactory handlerFactory, BrokerContext brokerContext, MqttContext mqttContext) {
        this.handlerFactory = (MqttHandlerFactory) handlerFactory;
        this.mqttProtocolHandler = new MqttProtocolHandler(brokerContext);
        this.mqttContext = mqttContext;
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
