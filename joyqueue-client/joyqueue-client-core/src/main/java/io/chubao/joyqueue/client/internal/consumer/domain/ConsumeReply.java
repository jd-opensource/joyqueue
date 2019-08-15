package io.chubao.joyqueue.client.internal.consumer.domain;

import io.chubao.joyqueue.network.command.RetryType;

/**
 * ConsumeReply
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/12
 */
public class ConsumeReply {

    private short partition;
    private long index;
    private RetryType retryType = RetryType.NONE;

    public ConsumeReply() {

    }

    public ConsumeReply(short partition, long index) {
        this.partition = partition;
        this.index = index;
    }

    public ConsumeReply(short partition, long index, RetryType retryType) {
        this.partition = partition;
        this.index = index;
        this.retryType = retryType;
    }

    public void setPartition(short partition) {
        this.partition = partition;
    }

    public short getPartition() {
        return partition;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getIndex() {
        return index;
    }

    public void setRetryType(RetryType retryType) {
        this.retryType = retryType;
    }

    public RetryType getRetryType() {
        return retryType;
    }

    @Override
    public String toString() {
        return "ConsumeReply{" +
                "partition=" + partition +
                ", index=" + index +
                ", retryType=" + retryType +
                '}';
    }
}