package io.chubao.joyqueue.broker.protocol.coordinator.domain;

import java.util.List;

/**
 * PartitionAssignment
 *
 * author: gaohaoxiang
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