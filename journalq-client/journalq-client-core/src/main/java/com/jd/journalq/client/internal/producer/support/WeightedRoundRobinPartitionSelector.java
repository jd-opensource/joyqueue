package com.jd.journalq.client.internal.producer.support;

import com.jd.journalq.client.internal.metadata.domain.PartitionMetadata;
import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import com.jd.journalq.client.internal.producer.domain.ProduceMessage;
import com.jd.journalq.common.network.domain.BrokerNode;
import com.jd.laf.extension.Extension;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WeightedRoundRobinPartitionSelector
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/27
 */
@Extension(singleton = false)
public class WeightedRoundRobinPartitionSelector extends AbstractPartitionSelector {

    private final AtomicInteger next = new AtomicInteger();
    private final AtomicInteger currentWeight = new AtomicInteger();
    private TopicMetadata lastTopicMetadata;

    @Override
    protected PartitionMetadata nextPartition(ProduceMessage message, TopicMetadata topicMetadata, List<PartitionMetadata> partitions) {
        int index = this.next.get();
        int currentWeight = this.currentWeight.get();
        int size = partitions.size();
        boolean weightChange = this.lastTopicMetadata != topicMetadata;

        if (weightChange) {
            this.lastTopicMetadata = topicMetadata;
        }

        if (index >= size) {
            this.next.set(1);
            index = 0;
        }

        while (true) {
            index = (index + 1) % size;
            if (index == 0 || weightChange) {
                weightChange = false;
                int maxGcd = getMaxGcd(partitions);
                currentWeight = currentWeight - maxGcd;
                if (currentWeight <= 0) {
                    int maxWeight = getMaxWeight(partitions);
                    currentWeight = maxWeight;
                    this.currentWeight.set(maxWeight);
                    this.next.set(index);
                    if(currentWeight == 0) {
                        return partitions.get(0);
                    }
                }
            }
            PartitionMetadata partition = partitions.get(index);
            BrokerNode partitionLeader = partition.getLeader();
            if (partitionLeader == null) {
                continue;
            }
            int partitionWeight = partitionLeader.getWeight();
            if(partitionWeight >= currentWeight) {
                this.currentWeight.set(currentWeight);
                this.next.set(index);
                return partition;
            }
        }
    }

    protected int getMaxGcd(List<PartitionMetadata> partitions) {
        int maxGcd = 0;
        for (int i = 0; i < partitions.size() - 1; i++) {
            BrokerNode leader = partitions.get(i).getLeader();
            BrokerNode leader1 = partitions.get(i + 1).getLeader();
            if (leader != null && leader1 != null) {
                maxGcd = getGcd(maxGcd, getGcd(leader.getWeight(), leader1.getWeight()));
            }
        }
        return maxGcd;
    }

    protected int getMaxWeight(List<PartitionMetadata> partitions) {
        int max = 0;
        for (PartitionMetadata partition : partitions) {
            if (partition.getLeader() == null) {
                continue;
            }
            int weight = partition.getLeader().getWeight();
            max = Math.max(weight, max);
        }
        return max;
    }

    protected int getGcd(int num1, int num2) {
        BigInteger i1 = new BigInteger(String.valueOf(num1));
        BigInteger i2 = new BigInteger(String.valueOf(num2));
        return i1.gcd(i2).intValue();
    }

    @Override
    public String type() {
        return "roundrobin";
    }
}