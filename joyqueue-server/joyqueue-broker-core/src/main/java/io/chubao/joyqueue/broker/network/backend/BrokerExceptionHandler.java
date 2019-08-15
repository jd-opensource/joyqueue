package io.chubao.joyqueue.broker.network.backend;

import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.handler.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 异常处理
 *
 * author: gaohaoxiang
 * date: 2018/9/17
 */
public class BrokerExceptionHandler implements ExceptionHandler {

    protected static final Logger logger = LoggerFactory.getLogger(BrokerExceptionHandler.class);

    @Override
    public void handle(Transport transport, Command command, Throwable throwable) {
        logger.error("broker transport exception, transport: {}, command: {}", transport, command, throwable);
    }
}