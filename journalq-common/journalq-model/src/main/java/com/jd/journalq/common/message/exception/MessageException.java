package com.jd.journalq.common.message.exception;

/**
 * 消息异常
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/9/4
 */
public class MessageException extends RuntimeException {

    public MessageException() {
    }

    public MessageException(String message) {
        super(message);
    }

    public MessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageException(Throwable cause) {
        super(cause);
    }

    public MessageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}