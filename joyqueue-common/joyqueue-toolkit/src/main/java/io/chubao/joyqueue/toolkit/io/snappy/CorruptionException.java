package io.chubao.joyqueue.toolkit.io.snappy;

import java.io.IOException;

/**
 * 异常
 */
public class CorruptionException extends IOException {
    public CorruptionException() {
    }

    public CorruptionException(String message) {
        super(message);
    }

    public CorruptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public CorruptionException(Throwable cause) {
        super(cause);
    }
}
