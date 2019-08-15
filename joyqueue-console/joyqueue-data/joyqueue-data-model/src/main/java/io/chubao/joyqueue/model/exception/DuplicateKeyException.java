package io.chubao.joyqueue.model.exception;

/**
 * 唯一约束异常
 * Created by hexiaofeng on 15-7-15.
 */
public class DuplicateKeyException extends RepositoryException {

    public DuplicateKeyException() {
    }

    public DuplicateKeyException(String message) {
        super(message);
    }

    public DuplicateKeyException(int status, String message) {
        super(status, message);
    }

    public DuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateKeyException(int status, String message, Throwable cause) {
        super(status, message, cause);
    }
}
