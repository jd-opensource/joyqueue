package com.jd.journalq.broker.election;


/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 227018/8/
 */
public class TopicPartitionGroup {
    public String topic;
    public int partitionGroupId;

    public TopicPartitionGroup() {}

    public TopicPartitionGroup(String topic, int partitionGroupId) {
        this.topic = topic;
        this.partitionGroupId = partitionGroupId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getPartitionGroupId() {
        return partitionGroupId;
    }

    @Override
    public int hashCode() {
        return topic.hashCode() + partitionGroupId;
    }

    @Override
    public boolean equals(Object object) {
        TopicPartitionGroup topicPartitionGroup = (TopicPartitionGroup)object;
        return topic.equals(topicPartitionGroup.getTopic()) &&
                partitionGroupId == topicPartitionGroup.getPartitionGroupId();
    }

    @Override
    public String toString() {
        return topic + "-" + partitionGroupId;
    }
}
