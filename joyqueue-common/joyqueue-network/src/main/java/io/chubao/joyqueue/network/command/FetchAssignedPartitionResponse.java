package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.Map;

/**
 * FetchAssignedPartitionResponse
 *
 * author: gaohaoxiang
 * date: 2018/12/4
 */
public class FetchAssignedPartitionResponse extends JoyQueuePayload {

    private Map<String, FetchAssignedPartitionAckData> topicPartitions;

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_ASSIGNED_PARTITION_RESPONSE.getCode();
    }

    public Map<String, FetchAssignedPartitionAckData> getTopicPartitions() {
        return topicPartitions;
    }

    public void setTopicPartitions(Map<String, FetchAssignedPartitionAckData> topicPartitions) {
        this.topicPartitions = topicPartitions;
    }
}