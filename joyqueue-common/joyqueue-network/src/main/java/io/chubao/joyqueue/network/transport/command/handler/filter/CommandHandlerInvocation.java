package io.chubao.joyqueue.network.transport.command.handler.filter;

import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandler;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.exception.TransportException;
import org.apache.commons.collections.CollectionUtils;

import java.util.Iterator;
import java.util.List;

/**
 * CommandHandlerInvocation
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/16
 */
public class CommandHandlerInvocation {

    private Transport transport;
    private Command request;
    private CommandHandler commandHandler;
    private Iterator<CommandHandlerFilter> filterIterator;
    private CommandHandlerContext context;

    public CommandHandlerInvocation(Transport transport, Command request, CommandHandler commandHandler, List<CommandHandlerFilter> filterList) {
        this.transport = transport;
        this.request = request;
        this.commandHandler = commandHandler;
        this.filterIterator = (CollectionUtils.isEmpty(filterList) ? null : filterList.iterator());
    }

    public Command invoke() throws TransportException {
        if (filterIterator == null || !filterIterator.hasNext()) {
            return commandHandler.handle(transport, request);
        } else {
            return filterIterator.next().invoke(this);
        }
    }

    public Transport getTransport() {
        return transport;
    }

    public Command getRequest() {
        return request;
    }

    public CommandHandlerContext getContext() {
        if (context == null) {
            context = new CommandHandlerContext();
        }
        return context;
    }
}