package io.chubao.joyqueue.model.exception;

/**
 * 状态异常
 * Created by hexiaofeng on 15-4-21.
 */
public class StateException extends BusinessException {

    protected StateException() {
    }

    public StateException(String message) {
        super(message);
    }

    public StateException(int status, String message) {
        super(status, message);
    }

    public StateException(String code, String message) {
        super(code, message);
    }

    public StateException(String message, Throwable cause) {
        super(message, cause);
    }

    public StateException(int status, String code, String message, Throwable cause) {
        super(status, code, message, cause);
    }
}
