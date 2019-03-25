package com.jd.journalq.nsr.network.command;

import com.jd.journalq.domain.TopicConfig;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.network.transport.command.JMQPayload;

import java.util.Map;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetTopicConfigByBrokerAck extends JMQPayload {
    private Map<TopicName, TopicConfig> topicConfigs;

    public GetTopicConfigByBrokerAck topicConfigs(Map<TopicName, TopicConfig> topicConfigs){
        this.topicConfigs = topicConfigs;
        return this;
    }

    public Map<TopicName, TopicConfig> getTopicConfigs() {
        return topicConfigs;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_TOPICCONFIGS_BY_BROKER_ACK;
    }
}
