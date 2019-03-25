package com.jd.journalq.client.internal.producer.exception;

/**
 * RetryException
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/26
 */
public class NeedRetryException extends ProducerException {

    public NeedRetryException() {
    }

    public NeedRetryException(String message) {
        super(message);
    }

    public NeedRetryException(String message, Throwable cause) {
        super(message, cause);
    }

    public NeedRetryException(Throwable cause) {
        super(cause);
    }

    public NeedRetryException(String error, int code) {
        super(error, code);
    }

    public NeedRetryException(String error, int code, Throwable cause) {
        super(error, code, cause);
    }
}