package com.jd.journalq.broker.kafka.command;


import com.google.common.collect.HashMultimap;
import com.jd.journalq.broker.kafka.KafkaCommandType;

/**
 * Created by zhangkepeng on 16-7-28.
 */
public class OffsetFetchRequest extends KafkaRequestOrResponse {
    private String groupId;
    private HashMultimap<String, Integer> topicAndPartitions;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public HashMultimap<String, Integer> getTopicAndPartitions() {
        return topicAndPartitions;
    }

    public void setTopicAndPartitions(HashMultimap<String, Integer> topicAndPartitions) {
        this.topicAndPartitions = topicAndPartitions;
    }

    @Override
    public int type() {
        return KafkaCommandType.OFFSET_FETCH.getCode();
    }

    @Override
    public String toString() {
        return describe();
    }

    public String describe() {
        StringBuilder offsetFetchRequest = new StringBuilder();
        offsetFetchRequest.append("Name: " + this.getClass().getSimpleName());
        offsetFetchRequest.append("; Version: " + getVersion());
        offsetFetchRequest.append("; CorrelationId: " + getCorrelationId());
        offsetFetchRequest.append("; ClientId: " + getClientId());
        offsetFetchRequest.append("; GroupId: " + groupId);
        return offsetFetchRequest.toString();
    }
}
