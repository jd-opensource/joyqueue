package io.chubao.joyqueue.broker.kafka.command;


import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.model.KafkaBroker;
import io.chubao.joyqueue.broker.kafka.model.KafkaTopicMetadata;

import java.util.List;

/**
 * Created by zhangkepeng on 16-7-29.
 */
public class TopicMetadataResponse extends KafkaRequestOrResponse {

    private List<KafkaTopicMetadata> topicMetadatas;
    private List<KafkaBroker> brokers;
    private String clusterId;

    public TopicMetadataResponse(List<KafkaTopicMetadata> topicMetadatas, List<KafkaBroker> brokers) {
        this.topicMetadatas = topicMetadatas;
        this.brokers = brokers;
    }

    public TopicMetadataResponse(List<KafkaTopicMetadata> topicMetadatas, List<KafkaBroker> brokers, String clusterId) {
        this.topicMetadatas = topicMetadatas;
        this.brokers = brokers;
        this.clusterId = clusterId;
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

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterId() {
        return clusterId;
    }

    @Override
    public int type() {
        return KafkaCommandType.METADATA.getCode();
    }

    @Override
    public String toString() {
        return "TopicMetadataResponse{" +
                "topicMetadatas=" + topicMetadatas +
                ", brokers=" + brokers +
                ", clusterId='" + clusterId + '\'' +
                '}';
    }
}
