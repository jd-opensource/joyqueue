package io.chubao.joyqueue.nsr.event;

import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.event.EventType;
import io.chubao.joyqueue.event.MetaEvent;

/**
 * RemoveProducerEvent
 * author: gaohaoxiang
 * date: 2019/8/29
 */
public class RemoveProducerEvent extends MetaEvent {

    private TopicName topic;
    private Producer producer;

    public RemoveProducerEvent() {

    }

    public RemoveProducerEvent(TopicName topic, Producer producer) {
        this.topic = topic;
        this.producer = producer;
    }

    public RemoveProducerEvent(EventType eventType, TopicName topic, Producer producer) {
        super(eventType);
        this.topic = topic;
        this.producer = producer;
    }

    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    public Producer getProducer() {
        return producer;
    }

    public void setProducer(Producer producer) {
        this.producer = producer;
    }

    @Override
    public String getTypeName() {
        return EventType.REMOVE_PRODUCER.name();
    }
}