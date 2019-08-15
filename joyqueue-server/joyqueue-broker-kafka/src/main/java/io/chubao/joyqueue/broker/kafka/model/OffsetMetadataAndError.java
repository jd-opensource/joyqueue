package io.chubao.joyqueue.broker.kafka.model;

/**
 * Created by zhangkepeng on 16-8-4.
 */
public class OffsetMetadataAndError {

    private int partition;
    private long offset;
    private String metadata = OffsetAndMetadata.NO_METADATA;
    private short error;

    public OffsetMetadataAndError(short error) {
        this.error = error;
    }

    public OffsetMetadataAndError(int partition, short error) {
        this.partition = partition;
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

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public void setError(short error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return String.format("OffsetMetadataAndError[%d,%s,%d]", offset, metadata, error);
    }

}