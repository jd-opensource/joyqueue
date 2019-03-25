package com.jd.journalq.store;

/**
 * @author liyue25
 * Date: 2018-12-12
 */
public class PositionOverflowException extends ReadException {
    private final long position;
    private final long right;

    public PositionOverflowException(long position, long right) {
        super(String.format("Read position %d should be less than store right position %d.", position, right));
        this.position = position;
        this.right = right;
    }

    public long getPosition() {
        return position;
    }

    public long getRight() {
        return right;
    }
}
