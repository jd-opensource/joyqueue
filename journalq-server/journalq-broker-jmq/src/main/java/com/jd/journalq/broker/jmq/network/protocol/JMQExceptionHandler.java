package com.jd.journalq.broker.jmq.network.protocol;

import com.jd.journalq.broker.jmq.exception.JMQException;
import com.jd.journalq.domain.QosLevel;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.handler.ExceptionHandler;
import com.jd.journalq.network.transport.exception.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * jmq异常处理
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/28
 */
public class JMQExceptionHandler implements ExceptionHandler {

    protected static final Logger logger = LoggerFactory.getLogger(JMQExceptionHandler.class);

    @Override
    public void handle(Transport transport, Command command, Throwable throwable) {
        logger.error("process command exception, header: {}, payload: {}, transport: {}",
                command.getHeader(), command.getPayload(), transport, throwable);

        if (command.getHeader().getQosLevel().equals(QosLevel.ONE_WAY)) {
            return;
        }

        try {
            int code = JMQCode.CN_UNKNOWN_ERROR.getCode();
            String error = null;

            if (throwable instanceof TransportException) {
                TransportException transportException = (TransportException) throwable;
                code = transportException.getCode();
                error = transportException.getMessage();
            } else if (throwable instanceof JMQException) {
                JMQException jmqException = (JMQException) throwable;
                code = jmqException.getCode();
                error = jmqException.getMessage();
            } else if (throwable instanceof JMQException) {
                JMQException jmqException = (JMQException) throwable;
                code = jmqException.getCode();
                error = jmqException.getMessage();
            }

            transport.acknowledge(command, BooleanAck.build(code, error));
        } catch (Exception e) {
            logger.error("acknowledge command exception, header: {}, payload: {}, transport: {}",
                    command.getHeader(), command.getPayload(), transport, e);
        }
    }
}