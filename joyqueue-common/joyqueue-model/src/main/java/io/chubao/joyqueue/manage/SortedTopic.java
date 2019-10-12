package io.chubao.joyqueue.manage;

/**
 *  排序的 topic
 **/
public class SortedTopic {
    private String topic;
    private long value;
    private int order;
    // partition group leader count
    private int partitionGroupLeaders;
    private int partitionGroups;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getPartitionGroupLeaders() {
        return partitionGroupLeaders;
    }

    public void setPartitionGroupLeaders(int partitionGroupLeaders) {
        this.partitionGroupLeaders = partitionGroupLeaders;
    }

    public int getPartitionGroups() {
        return partitionGroups;
    }

    public void setPartitionGroups(int partitionGroups) {
        this.partitionGroups = partitionGroups;
    }
}
