package io.chubao.joyqueue.broker.cluster.event;

import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.event.EventType;
import io.chubao.joyqueue.event.MetaEvent;

import java.util.Map;

/**
 * CompensateEvent
 * author: gaohaoxiang
 * date: 2019/10/18
 */
public class CompensateEvent extends MetaEvent {

    private Map<TopicName, TopicConfig> topics;

    public CompensateEvent() {

    }

    public CompensateEvent(Map<TopicName, TopicConfig> topics) {
        this.topics = topics;
    }

    public void setTopics(Map<TopicName, TopicConfig> topics) {
        this.topics = topics;
    }

    public Map<TopicName, TopicConfig> getTopics() {
        return topics;
    }

    @Override
    public String getTypeName() {
        return EventType.COMPENSATE.name();
    }
}