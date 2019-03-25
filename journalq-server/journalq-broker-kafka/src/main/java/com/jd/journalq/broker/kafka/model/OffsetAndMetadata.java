package com.jd.journalq.broker.kafka.model;

/**
 * OffsetAndMetadata
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/7
 */
public class OffsetAndMetadata {

    public static final long INVALID_OFFSET = -1L;
    public static final String NO_METADATA = "";

    private long offset;
    private String metadata;
    private long offsetCommitTime;

    public OffsetAndMetadata() {
    }

    public OffsetAndMetadata(long offset, String metadata) {
        this.offset = offset;
        this.metadata = metadata;
    }

    public OffsetAndMetadata(long offset, String metadata, long offsetCommitTime) {
        this.offset = offset;
        this.metadata = metadata;
        this.offsetCommitTime = offsetCommitTime;
    }

    public long getOffset() {
        return offset;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    public long getOffsetCommitTime() {
        return offsetCommitTime;
    }

    public void setOffsetCommitTime(long offsetCommitTime) {
        this.offsetCommitTime = offsetCommitTime;
    }
}

