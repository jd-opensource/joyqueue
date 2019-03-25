package com.jd.journalq.model.query;

import com.jd.journalq.model.domain.Subscribe;

public class QPartitionGroupMonitor {
    private int partition;
    private int partitionGroup;
    private Subscribe subscribe;

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public int getPartitionGroup() {
        return partitionGroup;
    }

    public void setPartitionGroup(int partitionGroup) {
        this.partitionGroup = partitionGroup;
    }

    public Subscribe getSubscribe() {
        return subscribe;
    }

    public void setSubscribe(Subscribe subscribe) {
        this.subscribe = subscribe;
    }
}
