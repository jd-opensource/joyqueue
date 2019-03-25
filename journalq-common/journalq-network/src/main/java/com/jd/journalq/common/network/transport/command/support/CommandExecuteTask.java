package com.jd.journalq.common.network.transport.command.support;

import com.jd.journalq.common.network.transport.command.Command;
import com.jd.journalq.common.network.transport.command.handler.CommandHandler;
import com.jd.journalq.common.network.transport.command.handler.ExceptionHandler;
import com.jd.journalq.common.network.transport.command.handler.filter.CommandHandlerFilter;
import com.jd.journalq.common.network.transport.command.handler.filter.CommandHandlerFilterFactory;
import com.jd.journalq.common.network.transport.command.handler.filter.CommandHandlerInvocation;
import com.jd.journalq.common.network.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 命令执行线程
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/14
 */
public class CommandExecuteTask implements Runnable {

    protected static final Logger logger = LoggerFactory.getLogger(CommandExecuteTask.class);

    private Transport transport;
    private Command command;
    private CommandHandler commandHandler;
    private CommandHandlerFilterFactory commandHandlerFilterFactory;
    private ExceptionHandler exceptionHandler;

    public CommandExecuteTask(Transport transport, Command command, CommandHandler commandHandler, CommandHandlerFilterFactory commandHandlerFilterFactory, ExceptionHandler exceptionHandler) {
        this.transport = transport;
        this.command = command;
        this.commandHandler = commandHandler;
        this.commandHandlerFilterFactory = commandHandlerFilterFactory;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void run() {
        try {
            List<CommandHandlerFilter> commandHandlerFilters = commandHandlerFilterFactory.getFilters();
            CommandHandlerInvocation commandHandlerInvocation = new CommandHandlerInvocation(transport, command, commandHandler, commandHandlerFilters);
            Command response = commandHandlerInvocation.invoke();

            if (response != null) {
                transport.acknowledge(command, response);
            }
        } catch (Throwable t) {
            logger.error("command handler exception, tratnsport: {}, command: {}", transport, command, t);

            if (exceptionHandler != null) {
                exceptionHandler.handle(transport, command, t);
            }
        }
    }
}