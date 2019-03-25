package com.jd.journalq.model.exception;

/**
 * 业务异常
 */
public class BusinessException extends DataException {

    // 错误码
    protected String code;

    protected BusinessException() {
    }

    public BusinessException(String message) {
        this(500, "InternalError", message);
    }

    public BusinessException(int status, String message) {
        this(status, "InternalError", message);
    }

    public BusinessException(String code, String message) {
        this(500, code, message);
    }

    public BusinessException(String message, Throwable cause) {
        this(500, "InternalError", message, cause);
    }

    public BusinessException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public BusinessException(int status, String code, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}