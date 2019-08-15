package io.chubao.joyqueue.broker.kafka.network.protocol;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.broker.BrokerContextAware;
import io.chubao.joyqueue.broker.kafka.KafkaCommandHandler;
import io.chubao.joyqueue.broker.kafka.KafkaContext;
import io.chubao.joyqueue.broker.kafka.KafkaContextAware;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandler;
import io.chubao.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;
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
        super.register(commandHandler);
    }

    @Override
    public void register(int type, CommandHandler commandHandler) {
        super.register(type, commandHandler);
    }
}