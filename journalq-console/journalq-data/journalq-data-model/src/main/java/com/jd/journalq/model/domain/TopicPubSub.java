package com.jd.journalq.model.domain;

import java.util.List;

public class TopicPubSub {

    private SlimTopic topic;
    private List<SlimApplication> producers;
    private List<SlimApplication> consumers;

    public SlimTopic getTopic() {
        return topic;
    }

    public void setTopic(SlimTopic topic) {
        this.topic = topic;
    }


    public List<SlimApplication> getProducers() {
        return producers;
    }

    public void setProducers(List<SlimApplication> producers) {
        this.producers = producers;
    }

    public List<SlimApplication> getConsumers() {
        return consumers;
    }

    public void setConsumers(List<SlimApplication> consumers) {
        this.consumers = consumers;
    }
}
