package com.jd.journalq.broker.network.backend;

import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.handler.ExceptionHandler;
import com.jd.journalq.network.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 异常处理
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/9/17
 */
public class BrokerExceptionHandler implements ExceptionHandler {

    protected static final Logger logger = LoggerFactory.getLogger(BrokerExceptionHandler.class);

    @Override
    public void handle(Transport transport, Command command, Throwable throwable) {
        logger.error("broker transport exception, transport: {}, command: {}", transport, command, throwable);
    }
}