package io.chubao.joyqueue.network.transport.command.handler;

import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.Transport;

/**
 * ExceptionHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/13
 */
public interface ExceptionHandler {

    void handle(Transport transport, Command command, Throwable throwable);
}