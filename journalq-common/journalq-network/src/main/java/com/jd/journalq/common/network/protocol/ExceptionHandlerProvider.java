package com.jd.journalq.common.network.protocol;

import com.jd.journalq.common.network.transport.command.handler.ExceptionHandler;

/**
 * 异常处理提供器
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/28
 */
public interface ExceptionHandlerProvider {

    ExceptionHandler getExceptionHandler();
}