package com.jd.journalq.nsr.network.command;

import com.jd.journalq.domain.TopicConfig;
import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetTopicConfigAck extends JMQPayload {
    private TopicConfig topicConfig;
    public GetTopicConfigAck topicConfig(TopicConfig topicConfig){
        this.topicConfig = topicConfig;
        return this;
    }

    public TopicConfig getTopicConfig() {
        return topicConfig;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_TOPICCONFIG_ACK;
    }
}
