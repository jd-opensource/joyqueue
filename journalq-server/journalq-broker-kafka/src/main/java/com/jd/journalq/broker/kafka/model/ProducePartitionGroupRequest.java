package com.jd.journalq.broker.kafka.model;

import com.jd.journalq.message.BrokerMessage;

import java.util.List;
import java.util.Map;

/**
 * ProducePartitionGroupRequest
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/9
 */
public class ProducePartitionGroupRequest {

    private List<Integer> partitions;
    private List<BrokerMessage> messages;
    private Map<Integer, List<BrokerMessage>> messageMap;

    public ProducePartitionGroupRequest() {

    }

    public ProducePartitionGroupRequest(List<Integer> partitions, List<BrokerMessage> messages, Map<Integer, List<BrokerMessage>> messageMap) {
        this.partitions = partitions;
        this.messages = messages;
        this.messageMap = messageMap;
    }

    public void setPartitions(List<Integer> partitions) {
        this.partitions = partitions;
    }

    public List<Integer> getPartitions() {
        return partitions;
    }

    public void setMessages(List<BrokerMessage> messages) {
        this.messages = messages;
    }

    public List<BrokerMessage> getMessages() {
        return messages;
    }

    public void setMessageMap(Map<Integer, List<BrokerMessage>> messageMap) {
        this.messageMap = messageMap;
    }

    public Map<Integer, List<BrokerMessage>> getMessageMap() {
        return messageMap;
    }
}