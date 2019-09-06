package io.chubao.joyqueue.nsr.event;

import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.event.EventType;
import io.chubao.joyqueue.event.MetaEvent;

/**
 * UpdateConsumerEvent
 * author: gaohaoxiang
 * date: 2019/8/29
 */
public class UpdateConsumerEvent extends MetaEvent {

    private TopicName topic;
    private Consumer oldConsumer;
    private Consumer newConsumer;

    public UpdateConsumerEvent() {

    }

    public UpdateConsumerEvent(TopicName topic, Consumer oldConsumer, Consumer newConsumer) {
        this.topic = topic;
        this.oldConsumer = oldConsumer;
        this.newConsumer = newConsumer;
    }

    public UpdateConsumerEvent(EventType eventType, TopicName topic, Consumer oldConsumer, Consumer newConsumer) {
        super(eventType);
        this.topic = topic;
        this.oldConsumer = oldConsumer;
        this.newConsumer = newConsumer;
    }

    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    public Consumer getOldConsumer() {
        return oldConsumer;
    }

    public void setOldConsumer(Consumer oldConsumer) {
        this.oldConsumer = oldConsumer;
    }

    public Consumer getNewConsumer() {
        return newConsumer;
    }

    public void setNewConsumer(Consumer newConsumer) {
        this.newConsumer = newConsumer;
    }

    @Override
    public String getTypeName() {
        return EventType.UPDATE_CONSUMER.name();
    }
}