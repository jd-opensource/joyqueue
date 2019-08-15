package io.chubao.joyqueue.network.protocol;

import io.chubao.joyqueue.network.transport.command.handler.ExceptionHandler;

/**
 * ExceptionHandlerProvider
 *
 * author: gaohaoxiang
 * date: 2018/8/28
 */
public interface ExceptionHandlerProvider {

    ExceptionHandler getExceptionHandler();
}