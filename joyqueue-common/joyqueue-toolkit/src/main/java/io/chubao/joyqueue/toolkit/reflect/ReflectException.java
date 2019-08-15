package io.chubao.joyqueue.toolkit.reflect;

/**
 * 验证异常
 * Created by hexiaofeng on 16-5-9.
 */
public class ReflectException extends RuntimeException {

    public ReflectException() {
    }

    public ReflectException(String message) {
        super(message);
    }

    public ReflectException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReflectException(Throwable cause) {
        super(cause);
    }
}
