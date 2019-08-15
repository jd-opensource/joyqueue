package io.chubao.joyqueue.event;

import io.chubao.joyqueue.domain.TopicName;

public class PartitionGroupEvent extends MetaEvent {
    private TopicName topic;
    private Integer partitionGroup;

    public PartitionGroupEvent() {
    }

    private PartitionGroupEvent(EventType type, TopicName topic, Integer partitionGroup) {
        super(type);
        this.topic = topic;
        this.partitionGroup = partitionGroup;
    }

    @Override
    public String getTypeName() {
        return getClass().getTypeName();
    }
    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    public void setPartitionGroup(Integer partitionGroup) {
        this.partitionGroup = partitionGroup;
    }



    public Integer getPartitionGroup() {
        return partitionGroup;
    }

    public static PartitionGroupEvent add(TopicName topic, Integer partitionGroup) {
        return new PartitionGroupEvent(EventType.ADD_PARTITION_GROUP, topic, partitionGroup);
    }

    public static PartitionGroupEvent update(TopicName topic, Integer partitionGroup) {
        return new PartitionGroupEvent(EventType.UPDATE_PARTITION_GROUP, topic, partitionGroup);
    }

    public static PartitionGroupEvent remove(TopicName topic, Integer partitionGroup) {
        return new PartitionGroupEvent(EventType.REMOVE_PARTITION_GROUP, topic, partitionGroup);
    }

    @Override
    public String toString() {
        return "PartitionGroupEvent{" +
                "topic='" + topic + '\'' +
                ", partitionGroup=" + partitionGroup +
                '}';
    }
}
