package io.chubao.joyqueue.store.file;

/**
 * @author liyue25
 * Date: 2018/9/14
 */
public class WriteException extends RuntimeException {
    public WriteException(String message) {
        super(message);
    }

    public WriteException() {
        super();
    }

    public WriteException(Throwable t) {
        super(t);
    }
}
