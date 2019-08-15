package io.chubao.joyqueue.network.transport.command.support;

import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandler;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import io.chubao.joyqueue.network.transport.command.handler.ExceptionHandler;
import io.chubao.joyqueue.network.transport.command.handler.filter.CommandHandlerFilterFactory;
import io.chubao.joyqueue.network.transport.command.provider.ExecutorServiceProvider;
import io.chubao.joyqueue.network.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/24
 */
public class RequestHandler {

    protected static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private CommandHandlerFactory commandHandlerFactory;
    private CommandHandlerFilterFactory commandHandlerFilterFactory;
    private ExceptionHandler exceptionHandler;

    public RequestHandler(CommandHandlerFactory commandHandlerFactory, CommandHandlerFilterFactory commandHandlerFilterFactory, ExceptionHandler exceptionHandler) {
        this.commandHandlerFactory = commandHandlerFactory;
        this.commandHandlerFilterFactory = commandHandlerFilterFactory;
        this.exceptionHandler = exceptionHandler;
    }

    public void handle(Transport transport, Command request) {
        CommandHandler commandHandler = commandHandlerFactory.getHandler(request);
        if (commandHandler == null) {
            logger.error("unsupported command, request: {}", request);
            return;
        }

        CommandExecuteTask commandExecuteTask = new CommandExecuteTask(transport, request, commandHandler, commandHandlerFilterFactory, exceptionHandler);

        try {
            if (commandHandler instanceof ExecutorServiceProvider) {
                ((ExecutorServiceProvider) commandHandler).getExecutorService(transport, request).execute(commandExecuteTask);
            } else {
                commandExecuteTask.run();
            }
        } catch (Throwable t) {
            logger.error("command handler exception, transport: {}, request: {}", transport, request, t);

            if (exceptionHandler != null) {
                exceptionHandler.handle(transport, request, t);
            }
        }
    }
}