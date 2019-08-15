package io.chubao.joyqueue.network.transport.command.handler;

import io.chubao.joyqueue.network.transport.command.Command;

/**
 * CommandHandlerFactory
 */
public interface CommandHandlerFactory {

    /**
     * 获取处理器，心跳命令，不支持的命令也需要返回默认处理器
     *
     * @param command 命令
     */
    CommandHandler getHandler(Command command);

}