package com.jd.journalq.event;

import com.jd.journalq.domain.TopicName;

public class ProducerEvent extends MetaEvent {
    private TopicName topic;
    private String app;

    public ProducerEvent() {
    }

    public ProducerEvent(EventType type,TopicName topic, String app) {
        super(type);
        this.app = app;
        this.topic = topic;
    }

    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }

    public static ProducerEvent add(TopicName topic, String app) {
        return new ProducerEvent(EventType.ADD_PRODUCER, topic, app);
    }

    public static ProducerEvent update(TopicName topic, String app) {
        return new ProducerEvent(EventType.UPDATE_PRODUCER, topic, app);
    }

    public static ProducerEvent remove(TopicName topic, String app) {
        return new ProducerEvent(EventType.REMOVE_PRODUCER, topic, app);
    }

    @Override
    public String toString() {
        return "ProducerEvent{" +
                "topic='" + topic + '\'' +
                ", app='" + app + '\'' +
                '}';
    }
}
