package com.jd.journalq.broker.kafka.command;


import com.jd.journalq.broker.kafka.KafkaCommandType;

import java.util.List;

/**
 * Created by zhangkepeng on 16-7-27.
 */
public class TopicMetadataRequest extends KafkaRequestOrResponse {

    private boolean allowAutoTopicCreation;
    private List<String> topics;

    public boolean isAllowAutoTopicCreation() {
        return allowAutoTopicCreation;
    }

    public void setAllowAutoTopicCreation(boolean allowAutoTopicCreation) {
        this.allowAutoTopicCreation = allowAutoTopicCreation;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public List<String> getTopics() {
        return topics;
    }

    @Override
    public int type() {
        return KafkaCommandType.METADATA.getCode();
    }

    @Override
    public String toString() {
        StringBuilder topicMetadataRequest = new StringBuilder();
        topicMetadataRequest.append("Name: " + this.getClass().getSimpleName());
        if (topics != null && !topics.isEmpty()) {
            for (String topic : topics) {
                topicMetadataRequest.append("; Topic: " + topic);
            }
        }
        return topicMetadataRequest.toString();
    }
}
