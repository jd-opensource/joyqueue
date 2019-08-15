package io.chubao.joyqueue.broker.protocol.exception;

import io.chubao.joyqueue.exception.JoyQueueCode;

/**
 * JoyQueueException
 *
 * author: gaohaoxiang
 * date: 2018/12/5
 */
public class JoyQueueException extends RuntimeException {

    private int code;

    public JoyQueueException(Throwable cause) {
        super(cause);
    }

    public JoyQueueException(int code) {
        this.code = code;
    }

    public JoyQueueException(String message, int code) {
        super(message);
        this.code = code;
    }

    public JoyQueueException(JoyQueueCode code, Object... args) {
        super(code.getMessage(args));
        this.code = code.getCode();
    }

    public JoyQueueException(JoyQueueCode code, Throwable cause, Object... args) {
        super(code.getMessage(args), cause);
        this.code = code.getCode();
    }

    public JoyQueueException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public JoyQueueException(Throwable cause, int code) {
        super(cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}