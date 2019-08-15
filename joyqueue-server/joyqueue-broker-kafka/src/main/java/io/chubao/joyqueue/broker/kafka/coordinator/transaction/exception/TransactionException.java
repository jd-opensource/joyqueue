package io.chubao.joyqueue.broker.kafka.coordinator.transaction.exception;

import io.chubao.joyqueue.broker.kafka.coordinator.exception.CoordinatorException;
import io.chubao.joyqueue.exception.JoyQueueCode;

/**
 * TransactionException
 *
 * author: gaohaoxiang
 * date: 2019/4/10
 */
public class TransactionException extends CoordinatorException {

    private int code;

    public TransactionException(int code) {
        this.code = code;
    }

    public TransactionException(String message, int code) {
        super(message);
        this.code = code;
    }

    public TransactionException(JoyQueueCode code, Object... args) {
        super(code.getMessage(args));
        this.code = code.getCode();
    }

    public TransactionException(JoyQueueCode code, Throwable cause, Object... args) {
        super(code.getMessage(args), cause);
        this.code = code.getCode();
    }

    public TransactionException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public TransactionException(Throwable cause, int code) {
        super(cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}