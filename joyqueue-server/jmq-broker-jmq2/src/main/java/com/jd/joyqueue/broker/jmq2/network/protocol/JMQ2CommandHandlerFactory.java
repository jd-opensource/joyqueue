package com.jd.joyqueue.broker.jmq2.network.protocol;

import com.google.common.collect.Lists;
import com.jd.joyqueue.broker.jmq2.JMQ2CommandHandler;
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
public class JMQ2CommandHandlerFactory extends DefaultCommandHandlerFactory {

    private BrokerContext brokerContext;
    private List<JMQ2CommandHandler> commandHandlers;

    public JMQ2CommandHandlerFactory(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
        this.commandHandlers = loadCommandHandlers();
        initCommandHandlers(commandHandlers);
        registerCommandHandlers(commandHandlers);
    }

    protected List<JMQ2CommandHandler> loadCommandHandlers() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(JMQ2CommandHandler.class));
    }

    protected void initCommandHandlers(List<JMQ2CommandHandler> commandHandlers) {
        for (JMQ2CommandHandler commandHandler : commandHandlers) {
            if (commandHandler instanceof BrokerContextAware) {
                ((BrokerContextAware) commandHandler).setBrokerContext(brokerContext);
            }
        }
    }

    protected void registerCommandHandlers(List<JMQ2CommandHandler> commandHandlers) {
        for (JMQ2CommandHandler commandHandler : commandHandlers) {
            super.register(commandHandler);
        }
    }
}