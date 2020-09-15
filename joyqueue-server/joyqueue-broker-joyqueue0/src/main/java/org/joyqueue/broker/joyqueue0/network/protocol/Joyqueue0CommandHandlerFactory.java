package org.joyqueue.broker.joyqueue0.network.protocol;

import com.google.common.collect.Lists;
import org.joyqueue.broker.joyqueue0.Joyqueue0CommandHandler;
import com.jd.laf.extension.ExtensionManager;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;

import java.util.List;

/**
 * JMQ2CommandHandlerFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/28
 */
public class Joyqueue0CommandHandlerFactory extends DefaultCommandHandlerFactory {

    private BrokerContext brokerContext;
    private List<Joyqueue0CommandHandler> commandHandlers;

    public Joyqueue0CommandHandlerFactory(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
        this.commandHandlers = loadCommandHandlers();
        initCommandHandlers(commandHandlers);
        registerCommandHandlers(commandHandlers);
    }

    protected List<Joyqueue0CommandHandler> loadCommandHandlers() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(Joyqueue0CommandHandler.class));
    }

    protected void initCommandHandlers(List<Joyqueue0CommandHandler> commandHandlers) {
        for (Joyqueue0CommandHandler commandHandler : commandHandlers) {
            if (commandHandler instanceof BrokerContextAware) {
                ((BrokerContextAware) commandHandler).setBrokerContext(brokerContext);
            }
        }
    }

    protected void registerCommandHandlers(List<Joyqueue0CommandHandler> commandHandlers) {
        for (Joyqueue0CommandHandler commandHandler : commandHandlers) {
            super.register(commandHandler);
        }
    }
}