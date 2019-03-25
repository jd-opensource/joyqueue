package com.jd.journalq.broker.jmq.network.protocol;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.jmq.JMQCommandHandler;
import com.jd.journalq.broker.jmq.JMQContext;
import com.jd.journalq.broker.jmq.JMQContextAware;
import com.jd.journalq.network.transport.command.support.DefaultCommandHandlerFactory;
import com.jd.laf.extension.ExtensionManager;

import java.util.List;

/**
 * JMQCommandHandlerFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/28
 */
public class JMQCommandHandlerFactory extends DefaultCommandHandlerFactory {

    private JMQContext jmqContext;
    private List<JMQCommandHandler> commandHandlers;

    public JMQCommandHandlerFactory(JMQContext jmqContext) {
        this.jmqContext = jmqContext;
        this.commandHandlers = loadCommandHandlers();
        initCommandHandlers(commandHandlers);
        registerCommandHandlers(commandHandlers);
    }

    protected List<JMQCommandHandler> loadCommandHandlers() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(JMQCommandHandler.class));
    }

    protected void initCommandHandlers(List<JMQCommandHandler> commandHandlers) {
        for (JMQCommandHandler commandHandler : commandHandlers) {
            if (commandHandler instanceof BrokerContextAware) {
                ((BrokerContextAware) commandHandler).setBrokerContext(jmqContext.getBrokerContext());
            }
            if (commandHandler instanceof JMQContextAware) {
                ((JMQContextAware) commandHandler).setJmqContext(jmqContext);
            }
        }
    }

    protected void registerCommandHandlers(List<JMQCommandHandler> commandHandlers) {
        for (JMQCommandHandler commandHandler : commandHandlers) {
            super.register(commandHandler);
        }
    }
}