package io.chubao.joyqueue.network.command;

/**
 * ProduceMessageAckItemData
 *
 * author: gaohaoxiang
 * date: 2018/12/19
 */
public class ProduceMessageAckItemData {

    public static final short INVALID_INDEX = -1;

    public static final short INVALID_PARTITION = -1;

    public static final short INVALID_START_TIME = -1;

    public static final ProduceMessageAckItemData INVALID_INSTANCE = new ProduceMessageAckItemData(INVALID_PARTITION, INVALID_INDEX, INVALID_START_TIME);

    private short partition;
    private long index;
    private long startTime;

    public ProduceMessageAckItemData() {

    }

    public ProduceMessageAckItemData(short partition, long index, long startTime) {
        this.partition = partition;
        this.index = index;
        this.startTime = startTime;
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

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}