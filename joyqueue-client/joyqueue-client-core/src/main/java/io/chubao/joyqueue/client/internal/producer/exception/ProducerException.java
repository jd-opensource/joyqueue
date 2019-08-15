package io.chubao.joyqueue.client.internal.producer.exception;

import io.chubao.joyqueue.client.internal.exception.ClientException;

/**
 * ProducerException
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/20
 */
public class ProducerException extends ClientException {

    public ProducerException() {
    }

    public ProducerException(String message) {
        super(message);
    }

    public ProducerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProducerException(Throwable cause) {
        super(cause);
    }

    public ProducerException(String error, int code) {
        super(error, code);
    }

    public ProducerException(String error, int code, Throwable cause) {
        super(error, code, cause);
    }
}