package io.chubao.joyqueue.client.internal.consumer.coordinator.domain;

import java.util.List;

/**
 * getPartitionAssignment
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/6
 */
public class PartitionAssignment {

    private List<Short> partitions;

    public PartitionAssignment() {

    }

    public PartitionAssignment(List<Short> partitions) {
        this.partitions = partitions;
    }

    public void setPartitions(List<Short> partitions) {
        this.partitions = partitions;
    }

    public List<Short> getPartitions() {
        return partitions;
    }

    @Override
    public String toString() {
        return "PartitionAssignment{" +
                "partitions=" + partitions +
                '}';
    }
}