package com.jd.journalq.client.internal.exception;

/**
 * SecurityException
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/16
 */
public class SecurityException extends RuntimeException {

    private int code;

    public SecurityException() {
    }

    public SecurityException(String message) {
        super(message);
    }

    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecurityException(Throwable cause) {
        super(cause);
    }

    public SecurityException(String error, int code) {
        super(error);
        this.code = code;
    }

    public SecurityException(String error, int code, Throwable cause) {
        super(error, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}