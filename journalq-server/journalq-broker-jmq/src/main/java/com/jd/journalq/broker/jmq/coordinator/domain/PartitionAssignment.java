package com.jd.journalq.broker.jmq.coordinator.domain;

import java.util.List;

/**
 * PartitionAssignment
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/5
 */
public class PartitionAssignment {

    private List<Short> partitions;

    public List<Short> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<Short> partitions) {
        this.partitions = partitions;
    }
}