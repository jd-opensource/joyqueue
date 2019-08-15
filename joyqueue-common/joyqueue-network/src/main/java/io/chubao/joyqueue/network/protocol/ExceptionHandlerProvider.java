package io.chubao.joyqueue.network.protocol;

import io.chubao.joyqueue.network.transport.command.handler.ExceptionHandler;

/**
 * ExceptionHandlerProvider
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/28
 */
public interface ExceptionHandlerProvider {

    ExceptionHandler getExceptionHandler();
}