package com.jd.journalq.client.internal.consumer.exception;

import com.jd.journalq.client.internal.exception.ClientException;

/**
 * ConsumerException
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/20
 */
public class ConsumerException extends ClientException {

    public ConsumerException() {
    }

    public ConsumerException(String message) {
        super(message);
    }

    public ConsumerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConsumerException(Throwable cause) {
        super(cause);
    }

    public ConsumerException(String error, int code) {
        super(error, code);
    }

    public ConsumerException(String error, int code, Throwable cause) {
        super(error, code, cause);
    }
}
