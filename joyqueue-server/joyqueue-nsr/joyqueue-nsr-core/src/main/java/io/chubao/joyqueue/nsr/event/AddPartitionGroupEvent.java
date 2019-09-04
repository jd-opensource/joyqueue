package io.chubao.joyqueue.nsr.event;

import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.event.EventType;
import io.chubao.joyqueue.event.MetaEvent;

/**
 * AddPartitionGroupEvent
 * author: gaohaoxiang
 * date: 2019/8/28
 */
public class AddPartitionGroupEvent extends MetaEvent {

    private TopicName topic;
    private PartitionGroup partitionGroup;

    public AddPartitionGroupEvent() {

    }

    public AddPartitionGroupEvent(TopicName topic, PartitionGroup partitionGroup) {
        this.topic = topic;
        this.partitionGroup = partitionGroup;
    }

    public AddPartitionGroupEvent(EventType eventType, TopicName topic, PartitionGroup partitionGroup) {
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
        return EventType.ADD_PARTITION_GROUP.name();
    }
}