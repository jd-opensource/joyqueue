package com.jd.journalq.network.command;

import com.jd.journalq.network.transport.command.JMQPayload;

import java.util.Map;

/**
 * FetchAssignedPartitionAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public class FetchAssignedPartitionAck extends JMQPayload {

    private Map<String, FetchAssignedPartitionAckData> topicPartitions;

    @Override
    public int type() {
        return JMQCommandType.FETCH_ASSIGNED_PARTITION_ACK.getCode();
    }

    public Map<String, FetchAssignedPartitionAckData> getTopicPartitions() {
        return topicPartitions;
    }

    public void setTopicPartitions(Map<String, FetchAssignedPartitionAckData> topicPartitions) {
        this.topicPartitions = topicPartitions;
    }
}