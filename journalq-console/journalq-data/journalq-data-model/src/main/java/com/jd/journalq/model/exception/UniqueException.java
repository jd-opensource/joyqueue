package com.jd.journalq.model.exception;

/**
 * 唯一性异常
 */
public class UniqueException extends BusinessException {

    protected UniqueException() {
    }

    public UniqueException(String message) {
        super(message);
    }

    public UniqueException(int status, String message) {
        super(status, message);
    }

    public UniqueException(String code, String message) {
        super(code, message);
    }

    public UniqueException(String message, Throwable cause) {
        super(message, cause);
    }

    public UniqueException(int status, String code, String message, Throwable cause) {
        super(status, code, message, cause);
    }

}