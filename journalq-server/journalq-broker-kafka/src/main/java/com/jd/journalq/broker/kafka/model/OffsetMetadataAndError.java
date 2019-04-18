package com.jd.journalq.broker.kafka.model;


import com.jd.journalq.broker.kafka.KafkaErrorCode;

/**
 * Created by zhangkepeng on 16-8-4.
 */
public class OffsetMetadataAndError {

    public static final OffsetMetadataAndError OFFSET_SYNC_FAIL = new OffsetMetadataAndError(OffsetAndMetadata.INVALID_OFFSET, OffsetAndMetadata.NO_METADATA, KafkaErrorCode.NOT_LEADER_FOR_PARTITION.getCode());
    public static final OffsetMetadataAndError OFFSET_SYNC_SUCCESS = new OffsetMetadataAndError(OffsetAndMetadata.INVALID_OFFSET, OffsetAndMetadata.NO_METADATA, KafkaErrorCode.NONE.getCode());

    private int partition;
    private long offset;
    private String metadata = OffsetAndMetadata.NO_METADATA;
    private short error;

    public OffsetMetadataAndError(short error) {
        this.error = error;
    }

    public OffsetMetadataAndError(long offset, String metadata, short error) {
        this.offset = offset;
        this.metadata = metadata;
        this.error = error;
    }

    public OffsetMetadataAndError(int partition, long offset, String metadata, short error) {
        this.partition = partition;
        this.offset = offset;
        this.metadata = metadata;
        this.error = error;
    }

    public OffsetMetadataAndError(int partition, long offset, short error) {
        this.partition = partition;
        this.offset = offset;
        this.error = error;
    }

    public int getPartition() {
        return partition;
    }

    public long getOffset() {
        return offset;
    }

    public String getMetadata() {
        return metadata;
    }

    public short getError() {
        return error;
    }

    @Override
    public String toString() {
        return String.format("OffsetMetadataAndError[%d,%s,%d]", offset, metadata, error);
    }

}