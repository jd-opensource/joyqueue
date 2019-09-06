package io.chubao.joyqueue.nsr.event;

import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.event.EventType;
import io.chubao.joyqueue.event.MetaEvent;

/**
 * UpdatePartitionGroupEvent
 * author: gaohaoxiang
 * date: 2019/8/28
 */
public class UpdatePartitionGroupEvent extends MetaEvent {

    private TopicName topic;
    private PartitionGroup oldPartitionGroup;
    private PartitionGroup newPartitionGroup;

    public UpdatePartitionGroupEvent() {

    }

    public UpdatePartitionGroupEvent(TopicName topic, PartitionGroup oldPartitionGroup, PartitionGroup newPartitionGroup) {
        this.topic = topic;
        this.oldPartitionGroup = oldPartitionGroup;
        this.newPartitionGroup = newPartitionGroup;
    }

    public UpdatePartitionGroupEvent(EventType eventType, TopicName topic, PartitionGroup oldPartitionGroup, PartitionGroup newPartitionGroup) {
        super(eventType);
        this.topic = topic;
        this.oldPartitionGroup = oldPartitionGroup;
        this.newPartitionGroup = newPartitionGroup;
    }

    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    public PartitionGroup getOldPartitionGroup() {
        return oldPartitionGroup;
    }

    public void setOldPartitionGroup(PartitionGroup oldPartitionGroup) {
        this.oldPartitionGroup = oldPartitionGroup;
    }

    public PartitionGroup getNewPartitionGroup() {
        return newPartitionGroup;
    }

    public void setNewPartitionGroup(PartitionGroup newPartitionGroup) {
        this.newPartitionGroup = newPartitionGroup;
    }

    @Override
    public String getTypeName() {
        return EventType.UPDATE_PARTITION_GROUP.name();
    }
}