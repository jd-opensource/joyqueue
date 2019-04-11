package com.jd.journalq.broker.kafka.coordinator.transaction.exception;

import com.jd.journalq.broker.kafka.coordinator.exception.CoordinatorException;
import com.jd.journalq.exception.JMQCode;

/**
 * CoordinatorTransactionException
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/10
 */
public class CoordinatorTransactionException extends CoordinatorException {

    private int code;

    public CoordinatorTransactionException(int code) {
        this.code = code;
    }

    public CoordinatorTransactionException(String message, int code) {
        super(message);
        this.code = code;
    }

    public CoordinatorTransactionException(JMQCode code, Object... args) {
        super(code.getMessage(args));
        this.code = code.getCode();
    }

    public CoordinatorTransactionException(JMQCode code, Throwable cause, Object... args) {
        super(code.getMessage(args), cause);
        this.code = code.getCode();
    }

    public CoordinatorTransactionException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public CoordinatorTransactionException(Throwable cause, int code) {
        super(cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}