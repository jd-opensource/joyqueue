package io.chubao.joyqueue.model.exception;

/**
 * 不存在异常
 * Created by hexiaofeng on 15-4-21.
 */
public class NotFoundException extends BusinessException {

    protected NotFoundException() {
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(int status, String message) {
        super(status, message);
    }

    public NotFoundException(String code, String message) {
        super(code, message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundException(int status, String code, String message, Throwable cause) {
        super(status, code, message, cause);
    }
}
