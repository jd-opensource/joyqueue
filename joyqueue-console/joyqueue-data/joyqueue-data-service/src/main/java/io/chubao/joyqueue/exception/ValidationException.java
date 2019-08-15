package io.chubao.joyqueue.exception;

import io.chubao.joyqueue.model.exception.BusinessException;

/**
 * 验证异常异常
 */
public class ValidationException extends BusinessException {

    public static final int UNIQUE_EXCEPTION_STATUS = 100;
    public static final int NOT_FOUND_EXCEPTION_STATUS = 200;
    public static final int OTHER_EXCEPTION_STATUS = 900;

    protected ValidationException() {
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(int status, String message) {
        super(status, message);
    }

    public ValidationException(String code, String message) {
        super(code, message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(int status, String code, String message, Throwable cause) {
        super(status, code, message, cause);
    }

}