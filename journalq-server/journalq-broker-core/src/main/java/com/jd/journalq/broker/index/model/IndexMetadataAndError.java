package com.jd.journalq.broker.index.model;

import com.jd.journalq.exception.JMQCode;

/**
 * Created by zhuduohui on 2018/9/17.
 */
public class IndexMetadataAndError {
    //public static final IndexMetadataAndError noOffset = new IndexMetadataAndError(IndexAndMetadata.INVALID_INDEX, IndexAndMetadata.NO_METADATA, ErrorCode.NO_ERROR);
    //public static final IndexMetadataAndError offsetsLoading = new IndexMetadataAndError(IndexAndMetadata.INVALID_INDEX, IndexAndMetadata.NO_METADATA, ErrorCode.OFFSETS_LOAD_INPROGRESS);
    //public static final IndexMetadataAndError notOffsetManagerForGroup = new IndexMetadataAndError(IndexAndMetadata.INVALID_INDEX, IndexAndMetadata.NO_METADATA, ErrorCode.NOT_COORDINATOR_FOR_CONSUMER);
    //public static final IndexMetadataAndError unknownTopicOrPartition = new IndexMetadataAndError(IndexAndMetadata.INVALID_INDEX, IndexAndMetadata.NO_METADATA, ErrorCode.UNKNOWN_TOPIC_OR_PARTITION);
    //public static final IndexMetadataAndError GroupCoordinatorNotAvailable = new IndexMetadataAndError(IndexAndMetadata.INVALID_INDEX, IndexAndMetadata.NO_METADATA, ErrorCode.GROUP_COORDINATOR_NOT_AVAILABLE);

    private long index;
    private String metadata = IndexAndMetadata.NO_METADATA;
    private short error = (short)JMQCode.SUCCESS.getCode();


    public IndexMetadataAndError(long index, String metadata, short error) {

        this.index = index;
        this.metadata = metadata;
        this.error = error;
    }

    public long getIndex() {
        return index;
    }

    public String getMetadata() {
        return metadata;
    }

    public short getError() {
        return error;
    }

    @Override
    public String toString() {
        return String.format("IndexMetadataAndError[%d,%s,%d]",index, metadata, error);
    }

}
