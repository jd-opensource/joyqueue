package com.jd.journalq.common.message;


import java.io.Serializable;

/**
 * 消息位置
 */
public class MessageLocation implements Serializable {
    // 主题
    protected String topic;
    // 分区
    protected short partition;
    // 序号
    private long index;

    public MessageLocation() {

    }

    public MessageLocation(String topic, short partition, long index) {
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(':').append(topic).append(':').append(partition).append(':').append(index);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        MessageLocation that = (MessageLocation) o;

        if (index != that.index) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (index ^ (index >>> 32));
        return result;
    }
}