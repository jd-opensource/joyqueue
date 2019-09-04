package io.chubao.joyqueue.nsr.event;

import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.event.EventType;
import io.chubao.joyqueue.event.MetaEvent;

/**
 * RemovePartitionGroupEvent
 * author: gaohaoxiang
 * date: 2019/8/28
 */
public class RemovePartitionGroupEvent extends MetaEvent {

    private TopicName topic;
    private PartitionGroup partitionGroup;

    public RemovePartitionGroupEvent() {

    }

    public RemovePartitionGroupEvent(TopicName topic, PartitionGroup partitionGroup) {
        this.topic = topic;
        this.partitionGroup = partitionGroup;
    }

    public RemovePartitionGroupEvent(EventType eventType, TopicName topic, PartitionGroup partitionGroup) {
        super(eventType);
        this.topic = topic;
        this.partitionGroup = partitionGroup;
    }

    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    public void setPartitionGroup(PartitionGroup partitionGroup) {
        this.partitionGroup = partitionGroup;
    }

    public PartitionGroup getPartitionGroup() {
        return partitionGroup;
    }

    @Override
    public String getTypeName() {
        return EventType.REMOVE_PARTITION_GROUP.name();
    }
}