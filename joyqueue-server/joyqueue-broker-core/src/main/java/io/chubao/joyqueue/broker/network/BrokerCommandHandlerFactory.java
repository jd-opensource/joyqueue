package io.chubao.joyqueue.broker.network;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.BrokerContextAware;
import io.chubao.joyqueue.broker.network.support.BrokerCommandHandlerRegistrar;
import io.chubao.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;
import com.jd.laf.extension.ExtensionManager;

import java.util.List;

/**
 * BrokerCommandHandlerFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/28
 */
public class BrokerCommandHandlerFactory extends DefaultCommandHandlerFactory {

    private BrokerContext brokerContext;
    private List<BrokerCommandHandler> commandHandlers;

    public BrokerCommandHandlerFactory(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
        this.commandHandlers = loadCommandHandlers();
        initCommandHandlers(commandHandlers);
        registerCommandHandlers(commandHandlers);
        BrokerCommandHandlerRegistrar.register(brokerContext, this);
    }

    protected List<BrokerCommandHandler> loadCommandHandlers() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(BrokerCommandHandler.class));
    }

    protected void initCommandHandlers(List<BrokerCommandHandler> commandHandlers) {
        for (BrokerCommandHandler commandHandler : commandHandlers) {
            if (commandHandler instanceof BrokerContextAware) {
                ((BrokerContextAware) commandHandler).setBrokerContext(brokerContext);
            }
        }
    }

    protected void registerCommandHandlers(List<BrokerCommandHandler> commandHandlers) {
        for (BrokerCommandHandler commandHandler : commandHandlers) {
            super.register(commandHandler);
        }
    }
}