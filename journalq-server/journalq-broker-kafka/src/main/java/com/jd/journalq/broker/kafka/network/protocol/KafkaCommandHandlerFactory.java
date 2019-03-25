package com.jd.journalq.broker.kafka.network.protocol;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.kafka.KafkaCommandHandler;
import com.jd.journalq.broker.kafka.KafkaContextAware;
import com.jd.journalq.broker.kafka.handler.AbstractKafkaCommandHandler;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.kafka.KafkaContext;
import com.jd.journalq.common.network.transport.command.handler.CommandHandler;
import com.jd.journalq.common.network.transport.command.support.DefaultCommandHandlerFactory;
import com.jd.laf.extension.ExtensionManager;

import java.util.List;

/**
 * KafkaCommandHandlerFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/14
 */
public class KafkaCommandHandlerFactory extends DefaultCommandHandlerFactory {

    private KafkaContext kafkaContext;
    private List<KafkaCommandHandler> commandHandlers;

    public KafkaCommandHandlerFactory(KafkaContext kafkaContext) {
        this.kafkaContext = kafkaContext;
        this.commandHandlers = loadCommandHandlers();
        initCommandHandlers(commandHandlers);
        registerCommandHandlers(commandHandlers);
    }

    protected List<KafkaCommandHandler> loadCommandHandlers() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(KafkaCommandHandler.class));
    }

    protected void initCommandHandlers(List<KafkaCommandHandler> commandHandlers) {
        for (KafkaCommandHandler commandHandler : commandHandlers) {
            if (commandHandler instanceof BrokerContextAware) {
                ((BrokerContextAware) commandHandler).setBrokerContext(kafkaContext.getBrokerContext());
            }
            if (commandHandler instanceof KafkaContextAware) {
                ((KafkaContextAware) commandHandler).setKafkaContext(kafkaContext);
            }
        }
    }

    protected void registerCommandHandlers(List<KafkaCommandHandler> commandHandlers) {
        for (KafkaCommandHandler commandHandler : commandHandlers) {
            register(commandHandler);
        }
    }

    @Override
    public void register(CommandHandler commandHandler) {
        if (commandHandler instanceof AbstractKafkaCommandHandler) {
            commandHandler = kafkaContext.getRateLimitHandlerFactory().create((AbstractKafkaCommandHandler) commandHandler);
        }
        super.register(commandHandler);
    }

    @Override
    public void register(int type, CommandHandler commandHandler) {
        if (commandHandler instanceof AbstractKafkaCommandHandler) {
            commandHandler = kafkaContext.getRateLimitHandlerFactory().create((AbstractKafkaCommandHandler) commandHandler);
        }
        super.register(type, commandHandler);
    }
}