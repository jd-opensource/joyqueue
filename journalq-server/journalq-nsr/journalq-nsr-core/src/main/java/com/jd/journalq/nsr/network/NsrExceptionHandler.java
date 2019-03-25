package com.jd.journalq.nsr.network;

import com.jd.journalq.common.network.transport.Transport;
import com.jd.journalq.common.network.transport.command.Command;
import com.jd.journalq.common.network.transport.command.handler.ExceptionHandler;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class NsrExceptionHandler implements ExceptionHandler {
    @Override
    public void handle(Transport transport, Command command, Throwable throwable) {

    }
}
