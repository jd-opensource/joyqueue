package com.jd.journalq.broker.election.command;

import com.jd.journalq.broker.election.TopicPartitionGroup;
import com.jd.journalq.network.transport.command.JMQPayload;
import com.jd.journalq.network.command.CommandType;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/15
 */
public class TimeoutNowRequest extends JMQPayload {
    private TopicPartitionGroup topicPartitionGroup;
    private int term;

    public TimeoutNowRequest(TopicPartitionGroup topicPartitionGroup, int term) {
        this.topicPartitionGroup = topicPartitionGroup;
        this.term = term;
    }

    public TopicPartitionGroup getTopicPartitionGroup() {
        return topicPartitionGroup;
    }

    public String getTopic() {
        return topicPartitionGroup.getTopic();
    }

    public int getPartitionGroup() {
        return topicPartitionGroup.getPartitionGroupId();
    }

    public int getTerm() {
        return term;
    }

    @Override
    public int type() {
        return CommandType.RAFT_TIMEOUT_NOW_REQUEST;
    }
}
