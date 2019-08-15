package io.chubao.joyqueue.network.transport.command;

import io.chubao.joyqueue.network.protocol.Protocol;

/**
 * CommandDispatcherFactory
 *
 * author: gaohaoxiang
 * date: 2018/8/16
 */
public interface CommandDispatcherFactory {

    CommandDispatcher getCommandDispatcher(Protocol protocol);
}