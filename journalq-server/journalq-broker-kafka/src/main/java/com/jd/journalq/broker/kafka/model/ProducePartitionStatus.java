package com.jd.journalq.broker.kafka.model;

/**
 * Created by zhangkepeng on 16-8-18.
 */
public class ProducePartitionStatus {

    public static final short NONE_OFFSET = 0;

    private int partition;
    private short errorCode;
    private long offset;

    public ProducePartitionStatus(short errorCode) {
        this.errorCode = errorCode;
    }

    public ProducePartitionStatus(short errorCode, long offset) {
        this.errorCode = errorCode;
        this.offset = offset;
    }

    public ProducePartitionStatus(int partition, long offset, short errorCode) {
        this.partition = partition;
        this.offset = offset;
        this.errorCode = errorCode;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public short getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(short errorCode) {
        this.errorCode = errorCode;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

}
