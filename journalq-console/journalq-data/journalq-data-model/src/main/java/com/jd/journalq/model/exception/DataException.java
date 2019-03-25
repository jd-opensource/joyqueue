package com.jd.journalq.model.exception;

/**
 * 数据异常
 */
public class DataException extends RuntimeException {
    // 代码
    protected int status;

    protected DataException() {
    }

    public DataException(String message) {
        super(message);
    }

    public DataException(int status, String message) {
        super(message);
        this.status = status;
    }

    public DataException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public int getStatus() {
        return this.status;
    }

}