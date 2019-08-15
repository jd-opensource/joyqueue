package io.chubao.joyqueue.broker.kafka.command;

import java.util.List;
import java.util.Map;

/**
 * SyncGroupAssignment
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/5
 */
public class SyncGroupAssignment {

    private byte[] userData;
    private Map<String, List<Integer>> topicPartitions;

    public byte[] getUserData() {
        return userData;
    }

    public void setUserData(byte[] userData) {
        this.userData = userData;
    }

    public void setTopicPartitions(Map<String, List<Integer>> topicPartitions) {
        this.topicPartitions = topicPartitions;
    }

    public Map<String, List<Integer>> getTopicPartitions() {
        return topicPartitions;
    }
}