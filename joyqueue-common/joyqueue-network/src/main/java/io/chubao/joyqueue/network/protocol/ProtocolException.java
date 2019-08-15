package io.chubao.joyqueue.network.protocol;

/**
 * ProtocolException
 *
 * author: gaohaoxiang
 * date: 2018/9/14
 */
public class ProtocolException extends RuntimeException {

    public ProtocolException() {
    }

    public ProtocolException(String message) {
        super(message);
    }

    public ProtocolException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtocolException(Throwable cause) {
        super(cause);
    }

    public ProtocolException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}