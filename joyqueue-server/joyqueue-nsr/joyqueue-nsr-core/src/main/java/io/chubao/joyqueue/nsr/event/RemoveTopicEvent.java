package io.chubao.joyqueue.nsr.event;

import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.Topic;
import io.chubao.joyqueue.event.EventType;
import io.chubao.joyqueue.event.MetaEvent;

import java.util.List;

/**
 * RemoveTopicEvent
 * author: gaohaoxiang
 * date: 2019/8/28
 */
public class RemoveTopicEvent extends MetaEvent {

    private Topic topic;
    private List<PartitionGroup> partitionGroups;

    public RemoveTopicEvent() {

    }

    public RemoveTopicEvent(Topic topic, List<PartitionGroup> partitionGroups) {
        this.topic = topic;
        this.partitionGroups = partitionGroups;
    }

    public RemoveTopicEvent(EventType eventType, Topic topic, List<PartitionGroup> partitionGroups) {
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
        return EventType.REMOVE_TOPIC.name();
    }
}