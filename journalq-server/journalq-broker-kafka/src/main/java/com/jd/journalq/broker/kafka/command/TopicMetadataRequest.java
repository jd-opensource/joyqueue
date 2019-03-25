package com.jd.journalq.broker.kafka.command;


import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.domain.TopicName;

import java.util.List;

/**
 * Created by zhangkepeng on 16-7-27.
 */
public class TopicMetadataRequest extends KafkaRequestOrResponse {

    private boolean allowAutoTopicCreation;
    private List<TopicName> topics;

    public boolean isAllowAutoTopicCreation() {
        return allowAutoTopicCreation;
    }

    public void setAllowAutoTopicCreation(boolean allowAutoTopicCreation) {
        this.allowAutoTopicCreation = allowAutoTopicCreation;
    }

    public void setTopics(List<TopicName> topics) {
        this.topics = topics;
    }

    public List<TopicName> getTopics() {
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
            for (TopicName topic : topics) {
                topicMetadataRequest.append("; Topic: " + topic);
            }
        }
        return topicMetadataRequest.toString();
    }
}
