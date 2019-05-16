package com.jd.journalq.broker.limit.exception;

/**
 * LimitException
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public class LimitException extends RuntimeException {

    public LimitException() {
    }

    public LimitException(String message) {
        super(message);
    }

    public LimitException(String message, Throwable cause) {
        super(message, cause);
    }

    public LimitException(Throwable cause) {
        super(cause);
    }

    public LimitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}