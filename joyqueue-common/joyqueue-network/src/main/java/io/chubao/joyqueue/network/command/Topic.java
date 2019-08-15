package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.domain.ConsumerPolicy;
import io.chubao.joyqueue.domain.ProducerPolicy;
import io.chubao.joyqueue.domain.TopicType;
import io.chubao.joyqueue.exception.JoyQueueCode;

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
    private ProducerPolicy producerPolicy;
    private ConsumerPolicy consumerPolicy;
    private TopicType type;
    private Map<Integer, TopicPartitionGroup> partitionGroups;
    private JoyQueueCode code;

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public ProducerPolicy getProducerPolicy() {
        return producerPolicy;
    }

    public void setProducerPolicy(ProducerPolicy producerPolicy) {
        this.producerPolicy = producerPolicy;
    }

    public ConsumerPolicy getConsumerPolicy() {
        return consumerPolicy;
    }

    public void setConsumerPolicy(ConsumerPolicy consumerPolicy) {
        this.consumerPolicy = consumerPolicy;
    }

    public TopicType getType() {
        return type;
    }

    public void setType(TopicType type) {
        this.type = type;
    }

    public Map<Integer, TopicPartitionGroup> getPartitionGroups() {
        return partitionGroups;
    }

    public void setPartitionGroups(Map<Integer, TopicPartitionGroup> partitionGroups) {
        this.partitionGroups = partitionGroups;
    }

    public void setCode(JoyQueueCode code) {
        this.code = code;
    }

    public JoyQueueCode getCode() {
        return code;
    }
}