package com.jd.journalq.broker.kafka.exception;

/**
 * KafkaException
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class KafkaException extends RuntimeException {

    public KafkaException() {
    }

    public KafkaException(String message) {
        super(message);
    }

    public KafkaException(String message, Throwable cause) {
        super(message, cause);
    }

    public KafkaException(Throwable cause) {
        super(cause);
    }

    public KafkaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
