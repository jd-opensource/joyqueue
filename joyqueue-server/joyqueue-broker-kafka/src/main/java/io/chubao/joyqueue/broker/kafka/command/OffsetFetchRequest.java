package io.chubao.joyqueue.broker.kafka.command;


import io.chubao.joyqueue.broker.kafka.KafkaCommandType;

import java.util.List;
import java.util.Map;

/**
 * Created by zhangkepeng on 16-7-28.
 */
public class OffsetFetchRequest extends KafkaRequestOrResponse {
    private String groupId;
    private Map<String, List<Integer>> topicAndPartitions;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setTopicAndPartitions(Map<String, List<Integer>> topicAndPartitions) {
        this.topicAndPartitions = topicAndPartitions;
    }

    public Map<String, List<Integer>> getTopicAndPartitions() {
        return topicAndPartitions;
    }

    @Override
    public int type() {
        return KafkaCommandType.OFFSET_FETCH.getCode();
    }

    @Override
    public String toString() {
        return "OffsetFetchRequest{" +
                "groupId='" + groupId + '\'' +
                ", topicAndPartitions=" + topicAndPartitions +
                '}';
    }
}
