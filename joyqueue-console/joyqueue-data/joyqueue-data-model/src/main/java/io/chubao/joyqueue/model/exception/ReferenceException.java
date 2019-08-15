package io.chubao.joyqueue.model.exception;

/**
 * 引用异常
 */
public class ReferenceException extends BusinessException {

    protected ReferenceException() {
    }

    public ReferenceException(String message) {
        super(message);
    }

    public ReferenceException(int status, String message) {
        super(status, message);
    }

    public ReferenceException(String code, String message) {
        super(code, message);
    }

    public ReferenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReferenceException(int status, String code, String message, Throwable cause) {
        super(status, code, message, cause);
    }
}