package io.chubao.joyqueue.nsr.event;

import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.event.EventType;
import io.chubao.joyqueue.event.MetaEvent;

/**
 * RemoveConsumerEvent
 * author: gaohaoxiang
 * date: 2019/8/29
 */
public class RemoveConsumerEvent extends MetaEvent {

    private TopicName topic;
    private Consumer consumer;

    public RemoveConsumerEvent() {

    }

    public RemoveConsumerEvent(TopicName topic, Consumer consumer) {
        this.topic = topic;
        this.consumer = consumer;
    }

    public RemoveConsumerEvent(EventType eventType, TopicName topic, Consumer consumer) {
        super(eventType);
        this.topic = topic;
        this.consumer = consumer;
    }

    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public String getTypeName() {
        return EventType.REMOVE_CONSUMER.name();
    }
}