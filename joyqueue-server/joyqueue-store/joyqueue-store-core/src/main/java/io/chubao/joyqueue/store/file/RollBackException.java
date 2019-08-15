package io.chubao.joyqueue.store.file;

/**
 * @author liyue25
 * Date: 2018/8/27
 */
public class RollBackException extends RuntimeException {
    public RollBackException(String message) {
        super(message);
    }
    public RollBackException(Throwable t) {
        super(t);
    }
}
