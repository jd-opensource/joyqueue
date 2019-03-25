package com.jd.journalq.client.internal.metadata.domain;

import com.jd.journalq.network.domain.BrokerNode;

import java.io.Serializable;
import java.util.Map;

/**
 * PartitionGroupMetadata
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/3
 */
public class PartitionGroupMetadata implements Serializable {

    private int id;
    private BrokerNode leader;
    private Map<Short, PartitionMetadata> partitions;

    public PartitionGroupMetadata(int id, BrokerNode leader, Map<Short, PartitionMetadata> partitions) {
        this.id = id;
        this.leader = leader;
        this.partitions = partitions;
    }

    public int getId() {
        return id;
    }

    public BrokerNode getLeader() {
        return leader;
    }

    public Map<Short, PartitionMetadata> getPartitions() {
        return partitions;
    }

    @Override
    public String toString() {
        return "PartitionGroupMetadata{" +
                "id=" + id +
                ", leader=" + leader +
                ", partitions=" + partitions +
                '}';
    }
}