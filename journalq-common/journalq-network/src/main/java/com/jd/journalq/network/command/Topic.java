package com.jd.journalq.network.command;

import com.jd.journalq.domain.Consumer;
import com.jd.journalq.domain.Producer;
import com.jd.journalq.exception.JMQCode;

import java.io.Serializable;
import java.util.Map;

/**
 * Topic
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/30
 */
public class Topic implements Serializable {

    private String topic;
    private Producer.ProducerPolicy producerPolicy;
    private Consumer.ConsumerPolicy consumerPolicy;
    private com.jd.journalq.domain.Topic.Type type;
    private Map<Integer, TopicPartitionGroup> partitionGroups;
    private JMQCode code;

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public Producer.ProducerPolicy getProducerPolicy() {
        return producerPolicy;
    }

    public void setProducerPolicy(Producer.ProducerPolicy producerPolicy) {
        this.producerPolicy = producerPolicy;
    }

    public Consumer.ConsumerPolicy getConsumerPolicy() {
        return consumerPolicy;
    }

    public void setConsumerPolicy(Consumer.ConsumerPolicy consumerPolicy) {
        this.consumerPolicy = consumerPolicy;
    }

    public com.jd.journalq.domain.Topic.Type getType() {
        return type;
    }

    public void setType(com.jd.journalq.domain.Topic.Type type) {
        this.type = type;
    }

    public Map<Integer, TopicPartitionGroup> getPartitionGroups() {
        return partitionGroups;
    }

    public void setPartitionGroups(Map<Integer, TopicPartitionGroup> partitionGroups) {
        this.partitionGroups = partitionGroups;
    }

    public void setCode(JMQCode code) {
        this.code = code;
    }

    public JMQCode getCode() {
        return code;
    }
}