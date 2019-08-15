package io.chubao.joyqueue.broker.kafka.coordinator.group.domain;

import io.chubao.joyqueue.broker.kafka.model.TopicAndPartition;

/**
 * GroupTopicPartition
 *
 * @author luoruiheng
 * @since 1/18/18
 */
public class GroupTopicPartition {

    private String groupId;
    private TopicAndPartition topicAndPartition;

    public GroupTopicPartition(String groupId, String topic, int partition) {
        this.groupId = groupId;
        this.topicAndPartition = new TopicAndPartition(topic, partition);
    }

    public GroupTopicPartition(String groupId, TopicAndPartition topicAndPartition) {
        this.groupId = groupId;
        this.topicAndPartition = topicAndPartition;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public TopicAndPartition getTopicAndPartition() {
        return topicAndPartition;
    }

    public void setTopicAndPartition(TopicAndPartition topicAndPartition) {
        this.topicAndPartition = topicAndPartition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupTopicPartition that = (GroupTopicPartition) o;

        if (!groupId.equals(that.groupId)) return false;
        return topicAndPartition.equals(that.topicAndPartition);
    }

    @Override
    public int hashCode() {
        int result = groupId.hashCode();
        result = 31 * result + topicAndPartition.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "GroupTopicPartition{" +
                "groupId='" + groupId + '\'' +
                ", topicAndPartition=" + topicAndPartition +
                '}';
    }
}
