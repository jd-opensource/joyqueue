package io.chubao.joyqueue.broker.protocol.network;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.broker.BrokerContextAware;
import io.chubao.joyqueue.broker.protocol.JoyQueueCommandHandler;
import io.chubao.joyqueue.broker.protocol.JoyQueueContext;
import io.chubao.joyqueue.broker.protocol.JoyQueueContextAware;
import io.chubao.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;
import com.jd.laf.extension.ExtensionManager;

import java.util.List;

/**
 * JoyQueueCommandHandlerFactory
 *
 * author: gaohaoxiang
 * date: 2018/11/28
 */
public class JoyQueueCommandHandlerFactory extends DefaultCommandHandlerFactory {

    private JoyQueueContext joyQueueContext;
    private List<JoyQueueCommandHandler> commandHandlers;

    public JoyQueueCommandHandlerFactory(JoyQueueContext joyQueueContext) {
        this.joyQueueContext = joyQueueContext;
        this.commandHandlers = loadCommandHandlers();
        initCommandHandlers(commandHandlers);
        registerCommandHandlers(commandHandlers);
    }

    protected List<JoyQueueCommandHandler> loadCommandHandlers() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(JoyQueueCommandHandler.class));
    }

    protected void initCommandHandlers(List<JoyQueueCommandHandler> commandHandlers) {
        for (JoyQueueCommandHandler commandHandler : commandHandlers) {
            if (commandHandler instanceof BrokerContextAware) {
                ((BrokerContextAware) commandHandler).setBrokerContext(joyQueueContext.getBrokerContext());
            }
            if (commandHandler instanceof JoyQueueContextAware) {
                ((JoyQueueContextAware) commandHandler).setJoyQueueContext(joyQueueContext);
            }
        }
    }

    protected void registerCommandHandlers(List<JoyQueueCommandHandler> commandHandlers) {
        for (JoyQueueCommandHandler commandHandler : commandHandlers) {
            super.register(commandHandler);
        }
    }
}