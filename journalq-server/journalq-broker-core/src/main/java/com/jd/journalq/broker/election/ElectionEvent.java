package com.jd.journalq.broker.election;


/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/20
 */
public class ElectionEvent {
    private Type eventType;
    private int term;
    private int leaderId;
    private TopicPartitionGroup topicPartitionGroup;

    public ElectionEvent(Type eventType, int term, int leaderId, TopicPartitionGroup topicPartitionGroup) {
        this.eventType = eventType;
        this.term = term;
        this.leaderId = leaderId;
        this.topicPartitionGroup = topicPartitionGroup;
    }
    public Type getEventType() {
        return eventType;
    }

    public int getTerm() {
        return term;
    }

    public int getLeaderId() {
        return leaderId;
    }

    public TopicPartitionGroup getTopicPartitionGroup() {
        return topicPartitionGroup;
    }

    public enum Type {
        START_ELECTION,// 开始选举
        LEADER_FOUND   // 找到Leader，leader为找到的Leader
    }
}
