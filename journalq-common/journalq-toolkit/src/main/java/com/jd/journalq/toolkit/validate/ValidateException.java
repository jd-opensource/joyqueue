package com.jd.journalq.toolkit.validate;

/**
 * 验证异常
 * Created by hexiaofeng on 16-5-9.
 */
public class ValidateException extends RuntimeException {

    public ValidateException() {
    }

    public ValidateException(String message) {
        super(message);
    }

    public ValidateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidateException(Throwable cause) {
        super(cause);
    }
}
