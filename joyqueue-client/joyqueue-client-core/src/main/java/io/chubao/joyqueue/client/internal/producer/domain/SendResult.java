package io.chubao.joyqueue.client.internal.producer.domain;

import java.io.Serializable;

/**
 * ProduceResult
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public class SendResult implements Serializable {

    private String topic;
    private short partition;
    private long index;
    private long startTime;

    public SendResult(String topic, short partition, long index, long startTime) {
        this.topic = topic;
        this.partition = partition;
        this.index = index;
        this.startTime = startTime;
    }

    public String getTopic() {
        return topic;
    }

    public short getPartition() {
        return partition;
    }

    @Deprecated
    public long getIndex() {
        return index;
    }

    public long getStartTime() {
        return startTime;
    }

    @Override
    public String toString() {
        return "SendResult{" +
                "topic='" + topic + '\'' +
                ", partition=" + partition +
                ", index=" + index +
                ", startTime=" + startTime +
                '}';
    }
}