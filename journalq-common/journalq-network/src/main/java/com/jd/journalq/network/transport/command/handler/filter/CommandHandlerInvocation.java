package com.jd.journalq.network.transport.command.handler.filter;

import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.handler.CommandHandler;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.exception.TransportException;
import org.apache.commons.collections.CollectionUtils;

import java.util.Iterator;
import java.util.List;

/**
 * 命令处理器上下文
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/16
 */
public class CommandHandlerInvocation {

    private Transport transport;
    private Command command;
    private CommandHandler commandHandler;
    private Iterator<CommandHandlerFilter> filterIterator;
    private CommandHandlerContext context;

    public CommandHandlerInvocation(Transport transport, Command command, CommandHandler commandHandler, List<CommandHandlerFilter> filterList) {
        this.transport = transport;
        this.command = command;
        this.commandHandler = commandHandler;
        this.filterIterator = (CollectionUtils.isEmpty(filterList) ? null : filterList.iterator());
    }

    public Command invoke() throws TransportException {
        if (filterIterator == null || !filterIterator.hasNext()) {
            return commandHandler.handle(transport, command);
        } else {
            return filterIterator.next().invoke(this);
        }
    }

    public Transport getTransport() {
        return transport;
    }

    public Command getCommand() {
        return command;
    }

    public CommandHandlerContext getContext() {
        if (context == null) {
            context = new CommandHandlerContext();
        }
        return context;
    }
}