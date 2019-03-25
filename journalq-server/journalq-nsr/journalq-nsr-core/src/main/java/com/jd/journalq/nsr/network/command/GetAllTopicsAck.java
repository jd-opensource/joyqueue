package com.jd.journalq.nsr.network.command;

import com.jd.journalq.network.transport.command.JMQPayload;

import java.util.Set;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetAllTopicsAck extends JMQPayload {

    private Set<String> topicNames;

    public GetAllTopicsAck topicNames(Set<String> topicNames){
        this.topicNames = topicNames;
        return this;
    }

    public Set<String> getTopicNames() {
        return topicNames;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_ALL_TOPICS_ACK;
    }
}
