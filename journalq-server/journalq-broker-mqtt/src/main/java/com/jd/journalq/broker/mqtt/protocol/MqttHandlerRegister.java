package com.jd.journalq.broker.mqtt.protocol;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.mqtt.command.MqttHandlerFactory;
import com.jd.journalq.broker.mqtt.handler.Handler;
import com.jd.journalq.common.network.transport.command.handler.CommandHandlerFactory;
import com.jd.laf.extension.ExtensionManager;

import java.util.List;

/**
 * @author majun8
 */
public class MqttHandlerRegister {
    public static CommandHandlerFactory register(MqttHandlerFactory mqttHandlerFactory) {
        List<Handler> handlers = loadCommandHandlers();
        mqttHandlerFactory.registers(handlers);
        return mqttHandlerFactory;
    }

    private static List<Handler> loadCommandHandlers() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(Handler.class));
    }
}
