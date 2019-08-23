package io.chubao.joyqueue.nsr.exception;

/**
 * NsrException
 * author: gaohaoxiang
 * date: 2019/8/23
 */
public class NsrException extends RuntimeException {

    public NsrException() {
    }

    public NsrException(String message) {
        super(message);
    }

    public NsrException(String message, Throwable cause) {
        super(message, cause);
    }

    public NsrException(Throwable cause) {
        super(cause);
    }

    public NsrException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
