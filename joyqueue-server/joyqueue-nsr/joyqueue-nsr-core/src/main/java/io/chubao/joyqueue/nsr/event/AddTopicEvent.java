package io.chubao.joyqueue.nsr.event;

import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.Topic;
import io.chubao.joyqueue.event.EventType;
import io.chubao.joyqueue.event.MetaEvent;

import java.util.List;

/**
 * AddTopicEvent
 * author: gaohaoxiang
 * date: 2019/8/28
 */
public class AddTopicEvent extends MetaEvent {

    private Topic topic;
    private List<PartitionGroup> partitionGroups;

    public AddTopicEvent() {

    }

    public AddTopicEvent(Topic topic, List<PartitionGroup> partitionGroups) {
        this.topic = topic;
        this.partitionGroups = partitionGroups;
    }

    public AddTopicEvent(EventType eventType, Topic topic, List<PartitionGroup> partitionGroups) {
        super(eventType);
        this.topic = topic;
        this.partitionGroups = partitionGroups;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public List<PartitionGroup> getPartitionGroups() {
        return partitionGroups;
    }

    public void setPartitionGroups(List<PartitionGroup> partitionGroups) {
        this.partitionGroups = partitionGroups;
    }

    @Override
    public String getTypeName() {
        return EventType.ADD_TOPIC.name();
    }
}