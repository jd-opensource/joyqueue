package com.jd.journalq.broker.kafka.model;


import com.jd.journalq.broker.kafka.KafkaErrorCode;

/**
 * Created by zhangkepeng on 16-8-4.
 */
public class OffsetMetadataAndError {

    private static final OffsetMetadataAndError noOffset = new OffsetMetadataAndError(OffsetAndMetadata.INVALID_OFFSET, OffsetAndMetadata.NO_METADATA, KafkaErrorCode.NONE);
    private static final OffsetMetadataAndError offsetsLoading = new OffsetMetadataAndError(OffsetAndMetadata.INVALID_OFFSET, OffsetAndMetadata.NO_METADATA, KafkaErrorCode.OFFSETS_LOAD_IN_PROGRESS);
    private static final OffsetMetadataAndError notOffsetManagerForGroup = new OffsetMetadataAndError(OffsetAndMetadata.INVALID_OFFSET, OffsetAndMetadata.NO_METADATA, KafkaErrorCode.NOT_COORDINATOR_FOR_CONSUMER);
    private static final OffsetMetadataAndError unknownTopicOrPartition = new OffsetMetadataAndError(OffsetAndMetadata.INVALID_OFFSET, OffsetAndMetadata.NO_METADATA, KafkaErrorCode.UNKNOWN_TOPIC_OR_PARTITION);

    private long offset;
    private String metadata = OffsetAndMetadata.NO_METADATA;
    private short error = KafkaErrorCode.NONE;

    public static final OffsetMetadataAndError OFFSET_SYNC_FAIL = new OffsetMetadataAndError(OffsetAndMetadata.INVALID_OFFSET, OffsetAndMetadata.NO_METADATA, KafkaErrorCode.UNKNOWN_TOPIC_OR_PARTITION);

    public OffsetMetadataAndError(short error) {
        this.error = error;
    }

    public OffsetMetadataAndError(long offset, String metadata, short error) {
        this.offset = offset;
        this.metadata = metadata;
        this.error = error;
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