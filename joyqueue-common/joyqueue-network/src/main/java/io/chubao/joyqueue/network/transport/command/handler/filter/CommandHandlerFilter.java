package io.chubao.joyqueue.network.transport.command.handler.filter;

import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.exception.TransportException;

/**
 * CommandHandlerFilter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/16
 */
public interface CommandHandlerFilter {

    Command invoke(CommandHandlerInvocation invocation) throws TransportException;
}