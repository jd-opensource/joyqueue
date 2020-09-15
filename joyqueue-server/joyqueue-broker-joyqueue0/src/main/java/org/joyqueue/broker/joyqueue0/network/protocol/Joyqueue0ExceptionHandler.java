package org.joyqueue.broker.joyqueue0.network.protocol;

import org.joyqueue.broker.joyqueue0.command.BooleanAck;
import org.joyqueue.domain.QosLevel;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.handler.ExceptionHandler;
import org.joyqueue.network.transport.exception.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionException;

/**
 * joyqueue0异常处理
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/28
 */
public class Joyqueue0ExceptionHandler implements ExceptionHandler {

    protected static final Logger logger = LoggerFactory.getLogger(Joyqueue0ExceptionHandler.class);

    @Override
    public void handle(Transport transport, Command command, Throwable throwable) {
        if (throwable instanceof RejectedExecutionException) {
            logger.error("process command exception, header: {}, payload: {}, transport: {}, exception: {}",
                    command.getHeader(), command.getPayload(), transport, throwable.getMessage());
        } else {
            logger.error("process command exception, header: {}, payload: {}, transport: {}",
                    command.getHeader(), command.getPayload(), transport, throwable);
        }

        if (command.getHeader().getQosLevel().equals(QosLevel.ONE_WAY)) {
            return;
        }

        try {
            int code = JoyQueueCode.CN_UNKNOWN_ERROR.getCode();
            String error = null;

            if (throwable instanceof TransportException) {
                TransportException transportException = (TransportException) throwable;
                code = transportException.getCode();
                error = transportException.getMessage();
            } else if (throwable instanceof RejectedExecutionException) {
                code = JoyQueueCode.CN_THREAD_EXECUTOR_BUSY.getCode();
                error = JoyQueueCode.CN_THREAD_EXECUTOR_BUSY.getMessage();
            }

            transport.acknowledge(command, BooleanAck.build(code, error));
        } catch (Exception e) {
            logger.error("acknowledge command exception, header: {}, payload: {}, transport: {}",
                    command.getHeader(), command.getPayload(), transport, e);
        }
    }
}