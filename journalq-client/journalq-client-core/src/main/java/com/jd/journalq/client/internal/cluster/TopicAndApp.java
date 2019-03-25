package com.jd.journalq.client.internal.cluster;

import com.jd.journalq.toolkit.lang.Objects;

import java.util.List;

public class TopicAndApp {

    private String topic;
    private List<String> topics;
    private String app;

    public TopicAndApp() {

    }

    public TopicAndApp(String topic, String app) {
        this.topic = topic;
        this.app = app;
    }

    public TopicAndApp(List<String> topics, String app) {
        this.topics = topics;
        this.app = app;
    }

    public String getTopic() {
        return topic;
    }

    public List<String> getTopics() {
        return topics;
    }

    public String getApp() {
        return app;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(topic, topics, app);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TopicAndApp)) {
            return false;
        }
        TopicAndApp target = (TopicAndApp) obj;
        return (target.getApp().equals(app)
                && Objects.equal(target.getTopic(), topic)
                && Objects.equal(target.getTopics(), topics));
    }
}