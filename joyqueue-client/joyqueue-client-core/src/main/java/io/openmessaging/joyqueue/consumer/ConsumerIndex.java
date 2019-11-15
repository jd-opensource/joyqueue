package io.openmessaging.joyqueue.consumer;

/**
 * ConsumerIndex
 * author: gaohaoxiang
 * date: 2019/11/12
 */
public class ConsumerIndex {

    private long index;
    private long leftIndex;
    private long rightIndex;

    public ConsumerIndex(long index, long leftIndex, long rightIndex) {
        this.index = index;
        this.leftIndex = leftIndex;
        this.rightIndex = rightIndex;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getLeftIndex() {
        return leftIndex;
    }

    public void setLeftIndex(long leftIndex) {
        this.leftIndex = leftIndex;
    }

    public long getRightIndex() {
        return rightIndex;
    }

    public void setRightIndex(long rightIndex) {
        this.rightIndex = rightIndex;
    }
}