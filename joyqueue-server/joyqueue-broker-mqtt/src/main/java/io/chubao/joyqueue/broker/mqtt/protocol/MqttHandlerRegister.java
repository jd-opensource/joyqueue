package io.chubao.joyqueue.broker.mqtt.protocol;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.broker.mqtt.command.MqttHandlerFactory;
import io.chubao.joyqueue.broker.mqtt.handler.Handler;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandlerFactory;
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
