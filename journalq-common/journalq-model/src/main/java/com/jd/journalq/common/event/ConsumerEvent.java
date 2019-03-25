package com.jd.journalq.common.event;

import com.jd.journalq.common.domain.TopicName;

public class ConsumerEvent extends MetaEvent {
    private TopicName topic;
    private String app;

    public ConsumerEvent() {
    }

    public ConsumerEvent(EventType type, TopicName topic, String app) {
        super(type);
        this.topic = topic;
        this.app = app;
    }

    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public static ConsumerEvent add(TopicName topic, String app) {
        return new ConsumerEvent(EventType.ADD_CONSUMER, topic, app);
    }

    public static ConsumerEvent update(TopicName topic, String app) {
        return new ConsumerEvent(EventType.UPDATE_CONSUMER, topic, app);
    }

    public static ConsumerEvent remove(TopicName topic, String app) {
        return new ConsumerEvent(EventType.REMOVE_CONSUMER, topic, app);
    }

    @Override
    public String toString() {
        return "ConsumerEvent{" +
                "topic='" + topic + '\'' +
                ", app='" + app + '\'' +
                '}';
    }
}
