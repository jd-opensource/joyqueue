package io.chubao.joyqueue.broker.kafka.network.protocol;

import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.handler.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KafkaExceptionHandler
 *
 * author: gaohaoxiang
 * date: 2019/3/27
 */
public class KafkaExceptionHandler implements ExceptionHandler {

    protected final Logger logger = LoggerFactory.getLogger(KafkaExceptionHandler.class);

    @Override
    public void handle(Transport transport, Command command, Throwable throwable) {
        logger.error("kafka exception, command: {}, transport: {}", command, transport, throwable);
        transport.stop();
    }
}