package io.chubao.joyqueue.nsr.exception;

/**
 * MessengerException
 * author: gaohaoxiang
 * date: 2019/8/28
 */
public class MessengerException extends NsrException {

    public MessengerException() {
    }

    public MessengerException(String message) {
        super(message);
    }

    public MessengerException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessengerException(Throwable cause) {
        super(cause);
    }

    public MessengerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
