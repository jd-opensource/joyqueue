package org.joyqueue.broker.network.protocol.support;

import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.handler.CommandHandler;
import org.joyqueue.network.transport.command.provider.ExecutorServiceProvider;

import java.util.concurrent.ExecutorService;

/**
 * ProtocolCommandHandler
 * author: gaohaoxiang
 * date: 2019/12/26
 */
public class CommandHandlerWrapper implements CommandHandler, ExecutorServiceProvider {

    private CommandHandler delegate;
    private ExecutorService threadPool;

    public CommandHandlerWrapper(CommandHandler delegate, ExecutorService threadPool) {
        this.delegate = delegate;
        this.threadPool = threadPool;
    }

    @Override
    public Command handle(Transport transport, Command command) {
        return delegate.handle(transport, command);
    }

    @Override
    public ExecutorService getExecutorService(Transport transport, Command command) {
        return threadPool;
    }
}