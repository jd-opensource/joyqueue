package io.chubao.joyqueue.broker.mqtt.command;

import io.chubao.joyqueue.broker.mqtt.handler.Handler;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandler;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author majun8
 */
public class MqttHandlerFactory implements CommandHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(MqttHandlerFactory.class);

    private List<Handler> handlers = new ArrayList<>();

    @Override
    public CommandHandler getHandler(Command command) {
        return null;
    }

    public void register(Handler handler) {
        logger.warn("register handler type: {}, handler: {}", handler.type(), handler);
        handlers.add(handler);
    }

    public void registers(List<Handler> handlers) {
        for (Handler handler : handlers) {
            register(handler);
        }
    }

    public List<Handler> getHandlers() {
        return handlers;
    }
}
