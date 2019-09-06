package io.chubao.joyqueue.nsr.event;

import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.event.EventType;
import io.chubao.joyqueue.event.MetaEvent;

/**
 * UpdateProducerEvent
 * author: gaohaoxiang
 * date: 2019/8/29
 */
public class UpdateProducerEvent extends MetaEvent {

    private TopicName topic;
    private Producer oldProducer;
    private Producer newProducer;

    public UpdateProducerEvent() {

    }

    public UpdateProducerEvent(TopicName topic, Producer oldProducer, Producer newProducer) {
        this.topic = topic;
        this.oldProducer = oldProducer;
        this.newProducer = newProducer;
    }

    public UpdateProducerEvent(EventType eventType, TopicName topic, Producer oldProducer, Producer newProducer) {
        super(eventType);
        this.topic = topic;
        this.oldProducer = oldProducer;
        this.newProducer = newProducer;
    }

    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    public Producer getOldProducer() {
        return oldProducer;
    }

    public void setOldProducer(Producer oldProducer) {
        this.oldProducer = oldProducer;
    }

    public Producer getNewProducer() {
        return newProducer;
    }

    public void setNewProducer(Producer newProducer) {
        this.newProducer = newProducer;
    }

    @Override
    public String getTypeName() {
        return EventType.UPDATE_PRODUCER.name();
    }
}