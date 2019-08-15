package io.chubao.joyqueue.broker.kafka.model;

import java.util.List;

/**
 * Created by zhangkepeng on 16-7-29.
 */
public class KafkaTopicMetadata {

    private String topic;
    private short errorCode;
    private List<KafkaPartitionMetadata> kafkaPartitionMetadata;

    public KafkaTopicMetadata(String topic, List<KafkaPartitionMetadata> kafkaPartitionMetadata, short errorCode) {
        this.topic = topic;
        this.errorCode = errorCode;
        this.kafkaPartitionMetadata = kafkaPartitionMetadata;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public short getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(short errorCode) {
        this.errorCode = errorCode;
    }

    public List<KafkaPartitionMetadata> getKafkaPartitionMetadata() {
        return kafkaPartitionMetadata;
    }

    public void setKafkaPartitionMetadata(List<KafkaPartitionMetadata> kafkaPartitionMetadata) {
        this.kafkaPartitionMetadata = kafkaPartitionMetadata;
    }

    @Override
    public String toString() {
        return "KafkaTopicMetadata{" +
                "topic='" + topic + '\'' +
                ", errorCode=" + errorCode +
                ", kafkaPartitionMetadata=" + kafkaPartitionMetadata +
                '}';
    }
}