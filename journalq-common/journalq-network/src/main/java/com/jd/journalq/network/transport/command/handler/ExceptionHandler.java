package com.jd.journalq.network.transport.command.handler;

import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.Transport;

/**
 * 异常处理器
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/13
 */
public interface ExceptionHandler {

    void handle(Transport transport, Command command, Throwable throwable);
}