package io.chubao.joyqueue.toolkit.security.auth;

/**
 * Created by hexiaofeng on 17-1-18.
 */
public class AuthException extends RuntimeException {

    public AuthException() {
    }

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthException(Throwable cause) {
        super(cause);
    }
}
