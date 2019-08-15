package io.chubao.joyqueue.store;

/**
 * @author liyue25
 * Date: 2018-12-12
 */
public class PositionUnderflowException extends ReadException {
    private final long position;
    private final long left;

    public PositionUnderflowException(long position, long left) {
        super(String.format("Read position %d should be greater than or equal to store left position %d.", position, left));
        this.position = position;
        this.left = left;
    }

    public long getPosition() {
        return position;
    }

    public long getLeft() {
        return left;
    }
}
