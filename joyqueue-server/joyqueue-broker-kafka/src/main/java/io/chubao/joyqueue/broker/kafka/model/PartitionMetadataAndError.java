package io.chubao.joyqueue.broker.kafka.model;


/**
 * Created by zhangkepeng on 16-8-4.
 */
public class PartitionMetadataAndError {

    private int partition;
    private short error;

    public PartitionMetadataAndError() {

    }

    public PartitionMetadataAndError(int partition, short error) {
        this.partition = partition;
        this.error = error;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public short getError() {
        return error;
    }

    public void setError(short error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "PartitionMetadataAndError{" +
                "partition=" + partition +
                ", error=" + error +
                '}';
    }
}