package io.chubao.joyqueue.store.index;

/**
 * @author liyue25
 * Date: 2018/9/17
 */
public class BuildIndexFailedException extends RuntimeException {
    public BuildIndexFailedException(IndexOutOfBoundsException e) {
        super(e);
    }

    public BuildIndexFailedException(String message) {
        super(message);
    }
}
