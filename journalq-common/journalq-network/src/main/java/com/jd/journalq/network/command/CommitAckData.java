package com.jd.journalq.network.command;

/**
 * CommitAckData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/12
 */
public class CommitAckData {

    private short partition;
    private long index;
    private RetryType retryType;

    public CommitAckData() {

    }

    public CommitAckData(short partition, long index, RetryType retryType) {
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

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public RetryType getRetryType() {
        return retryType;
    }

    public void setRetryType(RetryType retryType) {
        this.retryType = retryType;
    }
}