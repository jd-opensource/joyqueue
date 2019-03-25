package com.jd.journalq.broker.jmq.exception;

import com.jd.journalq.exception.JMQCode;

/**
 * JMQException
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/5
 */
public class JMQException extends RuntimeException {

    private int code;

    public JMQException(Throwable cause) {
        super(cause);
    }

    public JMQException(int code) {
        this.code = code;
    }

    public JMQException(String message, int code) {
        super(message);
        this.code = code;
    }

    public JMQException(JMQCode code, Object... args) {
        super(code.getMessage(args));
        this.code = code.getCode();
    }

    public JMQException(JMQCode code, Throwable cause, Object... args) {
        super(code.getMessage(args), cause);
        this.code = code.getCode();
    }

    public JMQException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public JMQException(Throwable cause, int code) {
        super(cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}