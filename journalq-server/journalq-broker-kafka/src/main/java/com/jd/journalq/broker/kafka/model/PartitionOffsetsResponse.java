package com.jd.journalq.broker.kafka.model;


import java.util.Set;

/**
 * Created by zhangkepeng on 16-8-4.
 */
public class PartitionOffsetsResponse {

    private short errorCode;
    private long timestamp;
    private long offset;

    public PartitionOffsetsResponse(short errorCode, long timestamp, long offset) {
        this.errorCode = errorCode;
        this.offset = offset;
        this.timestamp = timestamp;
    }

    public short getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(short errorCode) {
        this.errorCode = errorCode;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Name: " + this.getClass().getSimpleName());
        stringBuilder.append(",errorCode : " + errorCode);
        stringBuilder.append(",offset :" + offset);
        return stringBuilder.toString();
    }
}
