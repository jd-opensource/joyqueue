package io.chubao.joyqueue.network.command;

import java.io.Serializable;
import java.util.Map;

/**
 * TopicPartitionGroup
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/30
 */
public class TopicPartitionGroup implements Serializable {

    private int id;
    private int leader;
    private Map<Short, TopicPartition> partitions;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLeader(int leader) {
        this.leader = leader;
    }

    public int getLeader() {
        return leader;
    }

    public Map<Short, TopicPartition> getPartitions() {
        return partitions;
    }

    public void setPartitions(Map<Short, TopicPartition> partitions) {
        this.partitions = partitions;
    }

    @Override
    public String toString() {
        return "TopicPartitionGroup{" +
                "id=" + id +
                ", leader=" + leader +
                ", partitions=" + partitions +
                '}';
    }
}