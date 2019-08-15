package io.chubao.joyqueue.nsr.network;

import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.handler.ExceptionHandler;
import io.chubao.joyqueue.network.transport.exception.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class NsrExceptionHandler implements ExceptionHandler {

    protected static final Logger logger = LoggerFactory.getLogger(NsrExceptionHandler.class);

    @Override
    public void handle(Transport transport, Command command, Throwable throwable) {
        if (TransportException.isClosed(throwable)) {
            logger.warn("channel close, address: {}, message: {}", transport.remoteAddress(), throwable.getMessage());
        } else {
            logger.error("nameserver exception, transport: {}, command: {}", transport, command, throwable);
        }
        transport.stop();
    }
}
