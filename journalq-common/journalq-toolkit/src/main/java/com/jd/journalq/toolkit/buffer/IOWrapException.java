package com.jd.journalq.toolkit.buffer;

/**
 * Catalyst IO exception.
 */
public class IOWrapException extends RuntimeException {

    public IOWrapException() {
    }

    public IOWrapException(String message) {
        super(message);
    }

    public IOWrapException(String message, Throwable cause) {
        super(message, cause);
    }

    public IOWrapException(Throwable cause) {
        super(cause);
    }

}
