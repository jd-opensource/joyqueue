package io.chubao.joyqueue.server.archive.store.model;

/**
 * 归档位置
 * <p>
 * Created by chengzhiliang on 2018/12/13.
 */
public class AchivePosition {
    private String topic;
    private short partition;
    private long index;

    public AchivePosition(String topic, short partition, long index) {
        this.topic = topic;
        this.partition = partition;
        this.index = index;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public short getPartition() {
        return partition;
    }

    public void setPartition(short partition) {
        this.partition = partition;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }
}
