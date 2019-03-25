package com.jd.journalq.broker.kafka.command;

import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.message.KafkaBrokerMessage;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.common.domain.TopicName;

import java.util.List;

/**
 * Created by zhangkepeng on 16-7-27.
 */
public class ProduceRequest extends KafkaRequestOrResponse {
    private short requiredAcks;
    private int ackTimeoutMs;
    private String transactionalId;

    private Table<TopicName, Integer, List<KafkaBrokerMessage>> topicPartitionMessages;

    public short getRequiredAcks() {
        return requiredAcks;
    }

    public void setRequiredAcks(short requiredAcks) {
        this.requiredAcks = requiredAcks;
    }

    public void setAckTimeoutMs(int ackTimeoutMs) {
        this.ackTimeoutMs = ackTimeoutMs;
    }

    public int getAckTimeoutMs() {
        return ackTimeoutMs;
    }

    public String getTransactionalId() {
        return transactionalId;
    }

    public void setTransactionalId(String transactionalId) {
        this.transactionalId = transactionalId;
    }

    public Table<TopicName, Integer, List<KafkaBrokerMessage>> getTopicPartitionMessages() {
        return topicPartitionMessages;
    }

    public void setTopicPartitionMessages(Table<TopicName, Integer, List<KafkaBrokerMessage>> topicPartitionMessages) {
        this.topicPartitionMessages = topicPartitionMessages;
    }

    @Override
    public int type() {
        return KafkaCommandType.PRODUCE.getCode();
    }

    @Override
    public String toString() {
        return describe();
    }

    private String describe() {
        StringBuilder producerRequest = new StringBuilder();
        producerRequest.append("Name: " + this.getClass().getSimpleName());
        producerRequest.append("; RequiredAcks: " + requiredAcks);
        producerRequest.append("; AckTimeoutMs: " + ackTimeoutMs + " ms");
        return producerRequest.toString();
    }
}