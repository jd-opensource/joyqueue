package io.chubao.joyqueue.broker.kafka.message.exception;

import io.chubao.joyqueue.broker.kafka.exception.KafkaException;

/**
 * Created by zhangkepeng on 16-8-30.
 */
public class UnknownCodecException extends KafkaException {

    public UnknownCodecException(String message) {
        super(message);
    }

    public UnknownCodecException(String message, Throwable e) {
        super(message, e);
    }
}
