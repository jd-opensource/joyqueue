package com.jd.journalq.model.exception;

/**
 * 仓库异常
 */
public class RepositoryException extends DataException {

    protected RepositoryException() {
    }

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(int code, String message) {
        super(message);
        this.status = code;
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryException(int code, String message, Throwable cause) {
        super(message, cause);
        this.status = code;
    }

}