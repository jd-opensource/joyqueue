package io.chubao.joyqueue.broker.manage.exception;

/**
 * manage
 *
 * author: gaohaoxiang
 * date: 2018/10/12
 */
public class ManageException extends RuntimeException {

    public ManageException() {
    }

    public ManageException(String message) {
        super(message);
    }

    public ManageException(String message, Throwable cause) {
        super(message, cause);
    }

    public ManageException(Throwable cause) {
        super(cause);
    }

    public ManageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}