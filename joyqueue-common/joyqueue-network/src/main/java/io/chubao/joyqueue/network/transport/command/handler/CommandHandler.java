package io.chubao.joyqueue.network.transport.command.handler;

import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.exception.TransportException;
import io.chubao.joyqueue.network.transport.command.provider.ExecutorServiceProvider;

/**
 * 命令处理器抽象，具体的处理器实现类用于处理特定的命令类型。
 * 因此每种类型的命令应实现对应自己的处理器以实现具体的业务逻辑。
 *
 * 以下是扩展接口，可以自定义线程池
 * @see ExecutorServiceProvider
 */
public interface CommandHandler {

    /**
     * 命令处理方法，用于实现特定类型命令处理的逻辑。
     *
     * @param transport 通道
     * @param command   请求命令
     * @return 处理完请求后返回针对该请求的一个响应命令
     * @throws TransportException 处理过程中发生的所有异常在方法内部需要进行捕获并转化为TransportException抛出
     */
    Command handle(Transport transport, Command command);

}