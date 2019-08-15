package io.chubao.joyqueue.event;

import io.chubao.joyqueue.domain.TopicName;

public class TopicEvent extends MetaEvent {
    private TopicName topic;

    public TopicEvent() {
    }

    @Override
    public String getTypeName() {
        return getClass().getTypeName();
    }

    private TopicEvent(EventType type, TopicName topic) {
        super(type);
        this.topic = topic;
    }

    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    public static TopicEvent add(TopicName topic) {
        return new TopicEvent(EventType.ADD_TOPIC, topic);
    }

    public static TopicEvent update(TopicName topic) {
        return new TopicEvent(EventType.UPDATE_TOPIC, topic);
    }

    public static TopicEvent remove(TopicName topic) {
        return new TopicEvent(EventType.REMOVE_TOPIC, topic);
    }

    @Override
    public String toString() {
        return "TopicEvent{" +
                "topic='" + topic + '\'' +
                '}';
    }
}
