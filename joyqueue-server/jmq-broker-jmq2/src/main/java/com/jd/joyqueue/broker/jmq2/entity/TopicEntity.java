package com.jd.joyqueue.broker.jmq2.entity;

import com.google.common.collect.Maps;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.Producer;
import org.joyqueue.domain.Topic;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * topic实体
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/9/3
 */
public class TopicEntity {

    private String topic;
    private Integer importance;
    private Boolean archive;
    private Short queues;
    private Topic.Type type;
    private Set<String> groups = new HashSet<String>();
    private Map<String, Producer.ProducerPolicy> producers = Maps.newHashMap();
    private Map<String, Consumer.ConsumerPolicy> consumers = Maps.newHashMap();

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getImportance() {
        return importance;
    }

    public void setImportance(Integer importance) {
        this.importance = importance;
    }

    public Boolean getArchive() {
        return archive;
    }

    public void setArchive(Boolean archive) {
        this.archive = archive;
    }

    public Short getQueues() {
        return queues;
    }

    public void setQueues(Short queues) {
        this.queues = queues;
    }

    public Topic.Type getType() {
        return type;
    }

    public void setType(Topic.Type type) {
        this.type = type;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    public Map<String, Producer.ProducerPolicy> getProducers() {
        return producers;
    }

    public void setProducers(Map<String, Producer.ProducerPolicy> producers) {
        this.producers = producers;
    }

    public Map<String, Consumer.ConsumerPolicy> getConsumers() {
        return consumers;
    }

    public void setConsumers(Map<String, Consumer.ConsumerPolicy> consumers) {
        this.consumers = consumers;
    }
}