package com.jd.journalq.broker.kafka.command;


import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.model.KafkaBroker;
import com.jd.journalq.broker.kafka.model.KafkaTopicMetadata;

import java.util.List;

/**
 * Created by zhangkepeng on 16-7-29.
 */
public class TopicMetadataResponse extends KafkaRequestOrResponse {

    private List<KafkaTopicMetadata> topicMetadatas;
    private List<KafkaBroker> brokers;

    public TopicMetadataResponse(List<KafkaBroker> brokers, List<KafkaTopicMetadata> topicMetadatas) {
        this.brokers = brokers;
        this.topicMetadatas = topicMetadatas;
    }

    public List<KafkaTopicMetadata> getTopicMetadatas() {
        return topicMetadatas;
    }

    public void setTopicMetadatas(List<KafkaTopicMetadata> topicMetadatas) {
        this.topicMetadatas = topicMetadatas;
    }

    public List<KafkaBroker> getBrokers() {
        return brokers;
    }

    public void setBrokers(List<KafkaBroker> brokers) {
        this.brokers = brokers;
    }

    @Override
    public int type() {
        return KafkaCommandType.METADATA.getCode();
    }

    @Override
    public String toString() {
        StringBuilder responseStringBuilder = new StringBuilder();
        responseStringBuilder.append("Name: " + this.getClass().getSimpleName());
        return responseStringBuilder.toString();
    }
}
