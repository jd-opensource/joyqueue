package com.jd.journalq.client.internal.producer.domain;

import java.io.Serializable;

/**
 * ProduceResult
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public class ProduceResult implements Serializable {

    private String topic;
    private short partition;
    private long index;
    private long startTime;

    public ProduceResult(String topic, short partition, long index, long startTime) {
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

    public long getIndex() {
        return index;
    }

    public long getStartTime() {
        return startTime;
    }
}